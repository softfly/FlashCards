package pl.softfly.flashcards.ui.cards.select;

import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.ui.cards.drag_swipe.DragSwipeCardBaseViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class SelectCardBaseViewAdapter
        extends DragSwipeCardBaseViewAdapter {

    protected final Set<Card> selectedCards = new HashSet<>();

    protected boolean hintsOnce = true;

    public SelectCardBaseViewAdapter(SelectListCardsActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
    }

    /* -----------------------------------------------------------------------------------------
     * Adapter methods overridden
     * ----------------------------------------------------------------------------------------- */

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectCardViewHolder(onCreateView(parent), this);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        SelectCardViewHolder selectCardViewHolder = (SelectCardViewHolder) holder;
        if (isCardSelected(position)) {
            selectCardViewHolder.selectItemView();
        } else {
            holder.unfocusItemView();
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Select cards - features
     * ----------------------------------------------------------------------------------------- */

    public boolean isSelectionMode() {
        return !selectedCards.isEmpty();
    }

    public boolean isCardSelected(int position) {
        Card card = getItem(position);
        return card != null && selectedCards.contains(card);
    }

    /**
     * C_02_02 When no card is selected and long pressing on the card, select the card.
     */
    public void onCardInvertSelect(SelectCardViewHolder viewHolder) {
        if (isCardSelected(viewHolder.getBindingAdapterPosition())) {
            onCardUnselect(viewHolder);
        } else {
            onCardSelect(viewHolder);
        }
    }

    public void onCardSelect(SelectCardViewHolder holder) {
        holder.selectItemView();
        Card card = getItem(holder.getBindingAdapterPosition());
        selectedCards.add(card);
        if (selectedCards.size() == 1) {
            // Add selection mode options to the menu.
            getActivity().refreshMenuOnAppBar();
            if (hintsOnce) {
                showShortToastMessage("Single tap to select.");
                showShortToastMessage("Long press to display a pop-up menu.");
                hintsOnce= false;
            }
        }
    }

    protected void showShortToastMessage(String text) {
        Toast.makeText(
                getActivity(),
                text,
                Toast.LENGTH_SHORT
        ).show();
    }

    public void onCardUnselect(SelectCardViewHolder holder) {
        Card card = getItem(holder.getBindingAdapterPosition());
        selectedCards.remove(card);
        holder.unfocusItemView();
        if (selectedCards.isEmpty())
            // Remove selection mode options from the menu.
            getActivity().refreshMenuOnAppBar();
    }

    public void onClickDeselectAll() {
        if (!selectedCards.isEmpty()) {
            selectedCards.clear();
            // Remove selection mode options from the menu.
            getActivity().refreshMenuOnAppBar();
            // Clear background on items.
            this.notifyDataSetChanged();
        }
    }

    public void onClickDeleteSelected() {
        // Delete the items in the order from the end.
        List<Integer> removedPositions = selectedCards
                .stream()
                .sorted((o1, o2) -> o2.getOrdinal().compareTo(o1.getOrdinal()))
                .map(card -> getCurrentList().indexOf(card))
                .collect(Collectors.toList());

        Completable.fromAction(
                        () -> {
                            getCurrentList().removeAll(selectedCards);
                            getDeckDb().cardDao().delete(selectedCards);
                        })
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    List cardsToRevert = new ArrayList(selectedCards);
                    getActivity().runOnUiThread(
                            () -> {
                                removedPositions.stream().forEach(position -> notifyItemRemoved(position));
                                int startPosition = removedPositions.get(0);
                                int countItem = getItemCount() - startPosition + 1;
                                loadCards(startPosition, countItem);
                                selectedCards.clear();

                                Snackbar.make(getActivity().getListCardsView(),
                                                "The card has been deleted.",
                                                Snackbar.LENGTH_LONG)
                                        .setAction("Undo", v -> revertCards(cardsToRevert))
                                        .show();
                            }, this::onErrorOnClickDeleteSelected);

                    // Disable the toolbar menu in selection mode.
                    getActivity().refreshMenuOnAppBar();
                })
                .subscribe(() -> {}, this::onErrorOnClickDeleteSelected);
    }

    protected void onErrorOnClickDeleteSelected(Throwable e) {
        getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName()
                        + "OnClickDeleteSelectedCards",
                "Error while deleting the selected cards."
        );
    }

    private void revertCards(@NonNull List<Card> cards) {
        getDeckDb().cardDao().restoreAsync(cards)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    selectedCards.addAll(cards);
                    getActivity().runOnUiThread(() -> loadCards(), this::onErrorRevertCards);
                    // Enable the toolbar menu in selection mode.
                    getActivity().refreshMenuOnAppBar();
                })
                .subscribe(() -> {}, this::onErrorRevertCards);
    }

    protected void onErrorRevertCards(Throwable e) {
        getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName()
                        + "OnClickRevertSelectedCards",
                "Error while restoring the removed cards."
        );
    }

    /**
     * @param pasteAfterOrdinal ordinal = getAdapterPosition() + 1
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
                                cutCard = getDeckDb().cardDao().findById(cutCard.getId());
                                int previousOrdinal = cutCard.getOrdinal();
                                // Paste after the clicked card.
                                if (previousOrdinal > currentOrdinal) {
                                    currentOrdinal++;
                                }
                                if (previousOrdinal == currentOrdinal) {
                                    continue;
                                }
                                getDeckDb().cardDao().changeCardOrdinal(cutCard, currentOrdinal);
                            }

                            // Refresh ordinal numbers.
                            int[] selectedIds = selectedCards.stream().mapToInt(Card::getId).toArray();
                            selectedCards.clear();
                            selectedCards.addAll(getDeckDb().cardDao().findByIds(selectedIds));
                        })
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> loadCards(
                        minPosition, maxPosition - minPosition
                ))
                .subscribe(() -> {
                }, e -> getExceptionHandler().handleException(
                        e, getActivity().getSupportFragmentManager(),
                        this.getClass().getSimpleName() + "_OnClickPasteCards"
                ));
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    @Override
    public SelectListCardsActivity getActivity() {
        return (SelectListCardsActivity) super.getActivity();
    }
}
