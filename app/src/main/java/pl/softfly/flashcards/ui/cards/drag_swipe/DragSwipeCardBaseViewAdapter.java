package pl.softfly.flashcards.ui.cards.drag_swipe;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.ui.cards.standard.CardBaseViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;
import pl.softfly.flashcards.ui.cards.standard.ListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class DragSwipeCardBaseViewAdapter
        extends CardBaseViewAdapter {

    private ItemTouchHelper touchHelper;

    public DragSwipeCardBaseViewAdapter(ListCardsActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DragSwipeCardViewHolder(onCreateView(parent), this);
    }

    public void onMoveCard(int fromPosition, int toPosition) {
        Card card = getItem(fromPosition);
        getCurrentList().remove(card);
        getCurrentList().add(toPosition, card);
        notifyItemMoved(fromPosition, toPosition);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void moveCard(@NonNull Card cutCard, int toPosition) {
        Completable.fromAction(
                        () -> getDeckDb().cardDao().changeCardOrdinal(cutCard, toPosition))
                .subscribeOn(Schedulers.io())
                .doOnComplete(this::loadCards)
                .subscribe(() -> {
                }, e -> getExceptionHandler().handleException(
                        e, getActivity().getSupportFragmentManager(),
                        this.getClass().getSimpleName() + "_MoveCard"
                ));
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    public ItemTouchHelper getTouchHelper() {
        return touchHelper;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }
}
