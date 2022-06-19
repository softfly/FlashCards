package pl.softfly.flashcards.ui.cards.select;

import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.ui.cards.drag_swipe.DragSwipeCardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class SelectCardRecyclerViewAdapter
        extends DragSwipeCardRecyclerViewAdapter {

    protected final Set<Card> selectedCards = new HashSet<>();

    private final SelectListCardsActivity activity;

    public SelectCardRecyclerViewAdapter(SelectListCardsActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
        this.activity = activity;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectCardViewHolder(onCreateView(parent), this);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (isCardSelected(position)) {
            holder.itemView.setSelected(true);
            holder.itemView.setBackgroundColor(
                    MaterialColors.getColor(holder.itemView, R.attr.colorItemSelected)
            );
        } else {
            holder.itemView.setSelected(false);
            holder.itemView.setBackgroundColor(0);
        }
    }

    /**
     * C_02_02 When no card is selected and long pressing on the card, select the card.
     */
    public void onCardInvertSelect(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (isCardSelected(viewHolder.getBindingAdapterPosition())) {
            onCardUnselect(viewHolder);
        } else {
            onCardSelect(viewHolder);
        }
    }

    public boolean isCardSelected(int position) {
        Card card = getItem(position);
        return card != null && selectedCards.contains(card);
    }

    public void onCardSelect(@NonNull RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setSelected(true);
        viewHolder.itemView.setBackgroundColor(
                MaterialColors.getColor(viewHolder.itemView, R.attr.colorItemSelected)
        );
        Card card = getItem(viewHolder.getBindingAdapterPosition());
        selectedCards.add(card);
        if (selectedCards.size() == 1) {
            // Add selection mode options to the menu.
            activity.refreshMenuOnAppBar();

            Toast.makeText(
                    activity,
                    "Single tap to select.\nLong press to display a pop-up menu.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public void onCardUnselect(@NonNull RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setSelected(false);
        viewHolder.itemView.setBackgroundColor(0);
        Card card = getItem(viewHolder.getBindingAdapterPosition());
        selectedCards.remove(card);
        if (selectedCards.isEmpty())
            // Remove selection mode options from the menu.
            activity.refreshMenuOnAppBar();
    }

    public void onClickDeselectAll() {
        if (!selectedCards.isEmpty()) {
            selectedCards.clear();
            // Remove selection mode options from the menu.
            activity.refreshMenuOnAppBar();
            // Clear background on items.
            this.notifyDataSetChanged();
        }
    }

    public boolean isSelectionMode() {
        return !selectedCards.isEmpty();
    }

    public void onClickDeleteSelected() {
        Completable.fromAction(
                        () -> {
                            getCurrentList().removeAll(selectedCards);
                            deckDb.cardDao().delete(selectedCards);
                        })
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    List cardsToRevert = new ArrayList(selectedCards);

                    int first = selectedCards.stream()
                            .mapToInt(card -> card.getOrdinal())
                            .min()
                            .getAsInt();

                    Stream<Card> stream = selectedCards.stream()
                            .sorted((o1, o2) -> o2.getOrdinal().compareTo(o1.getOrdinal()));

                    activity.runOnUiThread(
                            () -> {
                                stream.forEach(card -> notifyItemRemoved(card.getOrdinal() - 1));
                                loadCards(first - 1, getItemCount() - first + 1);
                                selectedCards.clear();
                            }
                    );

                    activity.runOnUiThread(
                            () -> Snackbar.make(getActivity().findViewById(R.id.listCards),
                                            "The card has been deleted.",
                                            Snackbar.LENGTH_LONG)
                                    .setAction("Undo", v -> revertCards(cardsToRevert))
                                    .show());

                    // Disable the pop-up menu in selection mode.
                    activity.refreshMenuOnAppBar();
                })
                .subscribe(() -> {
                }, e -> exceptionHandler.handleException(
                        e, activity.getSupportFragmentManager(),
                        SelectCardRecyclerViewAdapter.class.getSimpleName()
                                + "OnClickDeleteSelectedCards",
                        "Error while removing the selected cards."
                ));
    }

    private void revertCards(@NonNull List<Card> cards) {
        deckDb.cardDao().restoreAsync(cards)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    selectedCards.addAll(cards);
                    activity.runOnUiThread(() -> loadCards());
                    // Enable the pop-up menu in selection mode.
                    activity.refreshMenuOnAppBar();
                })
                .subscribe(() -> {
                }, e -> exceptionHandler.handleException(
                        e, activity.getSupportFragmentManager(),
                        SelectCardRecyclerViewAdapter.class.getSimpleName()
                                + "OnClickRevertSelectedCards",
                        "Error while restoring the removed cards."
                ));
    }

    /**
     * @param pasteAfterOrdinal Remember ordinal = getAdapterPosition() + 1
     */
    public void onClickPasteCards(int pasteAfterOrdinal) {
        Integer minPosition = Math.min(pasteAfterOrdinal, selectedCards.stream()
                .mapToInt(Card::getOrdinal)
                .min()
                .getAsInt()) - 1;

        Integer maxPosition = Math.max(pasteAfterOrdinal, selectedCards.stream()
                .mapToInt(Card::getOrdinal)
                .max()
                .getAsInt());

        Completable.fromAction(
                        () -> {
                            int currentOrdinal = pasteAfterOrdinal;
                            List<Card> sorted = selectedCards.stream()
                                    .sorted(Comparator.comparing(Card::getOrdinal))
                                    .collect(Collectors.toList());
                            for (Card cutCard : sorted) {
                                // Refresh as the ordinal might have changed in previous iteration
                                cutCard = deckDb.cardDao().findById(cutCard.getId());
                                int previousOrdinal = cutCard.getOrdinal();
                                // Paste after the clicked card.
                                if (previousOrdinal > currentOrdinal) {
                                    currentOrdinal++;
                                }
                                if (previousOrdinal == currentOrdinal) {
                                    continue;
                                }
                                deckDb.cardDao().changeCardOrdinal(cutCard, currentOrdinal);
                            }

                            // Refresh ordinal numbers.
                            int[] selectedIds = selectedCards.stream().mapToInt(Card::getId).toArray();
                            selectedCards.clear();
                            selectedCards.addAll(deckDb.cardDao().findByIds(selectedIds));
                        })
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> loadCards(
                        minPosition, maxPosition - minPosition
                ))
                .subscribe(() -> {
                }, e -> exceptionHandler.handleException(
                        e, activity.getSupportFragmentManager(),
                        SelectCardRecyclerViewAdapter.class.getSimpleName() + "_OnClickPasteCards"
                ));
    }

    public SelectListCardsActivity getActivity() {
        return activity;
    }
}
