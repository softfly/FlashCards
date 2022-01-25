package pl.softfly.flashcards.ui.cards.drag_swipe;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.cards.standard.CardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;
import pl.softfly.flashcards.ui.cards.standard.ListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class DragSwipeCardRecyclerViewAdapter
        extends CardRecyclerViewAdapter {

    private ItemTouchHelper touchHelper;

    public DragSwipeCardRecyclerViewAdapter(ListCardsActivity activity, String deckName) {
        super(activity, deckName);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DragSwipeCardViewHolder(onCreateView(parent), this);
    }

    public void onCardMoveNoSave(int fromPosition, int toPosition) {
        Card card = getCards().get(fromPosition);
        getCards().remove(card);
        getCards().add(toPosition, card);
        notifyItemMoved(fromPosition, toPosition);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void moveCard(@NonNull Card cutCard, int toPosition) {
        Completable.fromAction(
                () -> deckDb.cardDao().changeCardOrdinal(cutCard, toPosition))
                .subscribeOn(Schedulers.io())
                .doOnComplete(this::loadCards)
                .subscribe(() -> {
                }, e -> ExceptionDialog.showExceptionDialog(
                        "Move Card", e,
                        getActivity().getSupportFragmentManager(),
                        "MoveCard"
                ));
    }

    public ItemTouchHelper getTouchHelper() {
        return touchHelper;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }
}
