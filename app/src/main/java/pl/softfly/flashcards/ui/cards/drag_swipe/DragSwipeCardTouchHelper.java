package pl.softfly.flashcards.ui.cards.drag_swipe;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class DragSwipeCardTouchHelper extends ItemTouchHelper.Callback {

    protected static final int NO_DRAG = -1;
    protected static final int DRAG_AFTER_POSITION = 1;

    private final DragSwipeCardBaseViewAdapter adapter;
    private int dragToPosition = NO_DRAG;

    public DragSwipeCardTouchHelper(DragSwipeCardBaseViewAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        super.clearView(recyclerView, viewHolder);
        CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        cardViewHolder.unfocusItemView();
        dragToPosition = NO_DRAG;
    }

    @Override
    public void onSelectedChanged(
            @Nullable RecyclerView.ViewHolder viewHolder,
            int actionState
    ) {
        super.onSelectedChanged(viewHolder, actionState);
        CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        switch (actionState) {
            case ItemTouchHelper.ACTION_STATE_DRAG:
            case ItemTouchHelper.ACTION_STATE_SWIPE:
                cardViewHolder.focusSingleTapItemView();
                break;
            case ItemTouchHelper.ACTION_STATE_IDLE:
                if (dragToPosition != -1) {
                    Card card = adapter.getItem(dragToPosition);
                    if (card.getOrdinal() != dragToPosition + DRAG_AFTER_POSITION) {
                        adapter.moveCard(card, dragToPosition + DRAG_AFTER_POSITION);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        dragToPosition = target.getBindingAdapterPosition();
        adapter.onMoveCard(viewHolder.getBindingAdapterPosition(), dragToPosition);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onClickDeleteCard(viewHolder.getBindingAdapterPosition());
    }

    @Override
    public boolean isLongPressDragEnabled() {
        /**
         * It is implemented by {@link DragSwipeCardViewHolder#onLongPress(MotionEvent)}
         */
        return false;
    }

    @Override
    public int getMovementFlags(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    protected DragSwipeCardBaseViewAdapter getAdapter() {
        return adapter;
    }
}
