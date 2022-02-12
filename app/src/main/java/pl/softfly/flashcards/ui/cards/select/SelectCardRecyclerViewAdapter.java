package pl.softfly.flashcards.ui.cards.select;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.cards.drag_swipe.DragSwipeCardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class SelectCardRecyclerViewAdapter
        extends DragSwipeCardRecyclerViewAdapter {

    protected final Set<Card> selectedCards = new HashSet<>();

    private SelectListCardsActivity activity;

    public SelectCardRecyclerViewAdapter(SelectListCardsActivity activity, String deckName) {
        super(activity, deckName);
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
        if (selectedCards.contains(getCards().get(position))) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(0);
        }
    }

    public void onCardInvertSelect(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (isCardSelected(viewHolder.getAdapterPosition())) {
            onCardUnselect(viewHolder);
        } else {
            onCardSelect(viewHolder);
        }
    }

    public boolean isCardSelected(int position) {
        Card card = getCards().get(position);
        return card != null && selectedCards.contains(card);
    }

    public void onCardSelect(@NonNull RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
        Card card = getCards().get(viewHolder.getAdapterPosition());
        selectedCards.add(card);
        if (selectedCards.size() == 1)
            // Add selection mode options to the menu.
            activity.refreshMenuOnAppBar();
    }

    public void onCardUnselect(@NonNull RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(0);
        Card card = getCards().get(viewHolder.getAdapterPosition());
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
                    getCards().remove(selectedCards);
                    deckDb.cardDao().delete(selectedCards);
                })
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    Integer minPosition = selectedCards.stream()
                            .mapToInt(Card::getOrdinal)
                            .min()
                            .getAsInt() - 1;
                    loadCards(minPosition, getItemCount());
                    selectedCards.clear();
                    // Disable the pop-up menu in selection mode.
                    activity.refreshMenuOnAppBar();
                    activity.runOnUiThread(
                            () -> Toast.makeText(
                                    activity,
                                    "The cards have been deleted.",
                                    Toast.LENGTH_SHORT
                            ).show());
                })
                .subscribe(() -> {
                }, e -> exceptionHandler.handleException(
                        e, activity.getSupportFragmentManager(),
                        SelectCardRecyclerViewAdapter.class.getSimpleName() + "OnClickDeleteSelected"
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
