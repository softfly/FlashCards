package pl.softfly.flashcards.ui.cards.drag_swipe;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.entity.Card;

/**
 * @author Grzegorz Ziemski
 */
public class DragSwipeCardTouchHelper extends ItemTouchHelper.Callback {

    private final DragSwipeCardRecyclerViewAdapter adapter;
    private int dragTo = -1;

    public DragSwipeCardTouchHelper(DragSwipeCardRecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        super.clearView(recyclerView, viewHolder);
        dragTo = -1;
    }

    @Override
    public void onSelectedChanged(
            @Nullable RecyclerView.ViewHolder viewHolder,
            int actionState
    ) {
        super.onSelectedChanged(viewHolder, actionState);
        switch (actionState) {
            case ItemTouchHelper.ACTION_STATE_DRAG:
            case ItemTouchHelper.ACTION_STATE_SWIPE:
                setFocusColorItem(viewHolder.itemView);
                break;
            case ItemTouchHelper.ACTION_STATE_IDLE:
                if (dragTo != -1) {
                    Card card = adapter.getItem(dragTo);
                    if (card.getOrdinal() != dragTo + 1) {
                        adapter.moveCard(card, dragTo + 1);
                    }
                }
                break;
        }
    }

    private void setFocusColorItem(@NonNull View itemView) {
        itemView.setBackgroundColor(Color.GRAY);
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

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        dragTo = target.getAdapterPosition();
        adapter.onCardMoveNoSave(viewHolder.getAdapterPosition(), dragTo);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onClickDeleteCard(viewHolder.getAdapterPosition());
    }
}
