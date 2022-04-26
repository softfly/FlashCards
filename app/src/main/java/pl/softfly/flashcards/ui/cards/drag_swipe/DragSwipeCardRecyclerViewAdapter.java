package pl.softfly.flashcards.ui.cards.drag_swipe;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.cards.standard.CardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;
import pl.softfly.flashcards.ui.cards.standard.ListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class DragSwipeCardRecyclerViewAdapter
        extends CardRecyclerViewAdapter {

    private ItemTouchHelper touchHelper;

    protected ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();

    public DragSwipeCardRecyclerViewAdapter(ListCardsActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DragSwipeCardViewHolder(onCreateView(parent), this);
    }

    public void onCardMoveNoSave(int fromPosition, int toPosition) {
        Card card = getItem(fromPosition);
        getCurrentList().remove(card);
        getCurrentList().add(toPosition, card);
        notifyItemMoved(fromPosition, toPosition);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void moveCard(@NonNull Card cutCard, int toPosition) {
        Completable.fromAction(
                () -> deckDb.cardDao().changeCardOrdinal(cutCard, toPosition))
                .subscribeOn(Schedulers.io())
                .doOnComplete(this::loadCards)
                .subscribe(() -> {
                }, e -> exceptionHandler.handleException(
                        e, getActivity().getSupportFragmentManager(),
                        DragSwipeCardRecyclerViewAdapter.class.getSimpleName() + "MoveCard"
                ));
    }

    public ItemTouchHelper getTouchHelper() {
        return touchHelper;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }
}
