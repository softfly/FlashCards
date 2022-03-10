package pl.softfly.flashcards.ui.cards.select;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.ui.cards.drag_swipe.DragSwipeCardTouchHelper;

/**
 * @todo desc algoritm
 *
 * @author Grzegorz Ziemski
 */
public class SelectCardTouchHelper extends DragSwipeCardTouchHelper {

    private boolean isLongPress;

    private SelectCardRecyclerViewAdapter adapter;

    public SelectCardTouchHelper(SelectCardRecyclerViewAdapter adapter) {
        super(adapter);
        this.adapter = adapter;
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        if (isLongPress) {
            if (adapter.isSelectionMode()) {
                // C_02_04 When any card is selected and long pressing on the card, show the selected popup menu.
                ((SelectCardViewHolder) viewHolder).showSelectPopupMenu();
            } else {
                // C_02_02 When no card is selected and long pressing on the card, select the card.
                adapter.onCardInvertSelect(viewHolder);
            }
        }
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public void onSelectedChanged(
            @Nullable RecyclerView.ViewHolder viewHolder,
            int actionState
    ) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            isLongPress = true;
        }
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        isLongPress = false;
        return super.onMove(recyclerView,viewHolder,target);
    }
}
