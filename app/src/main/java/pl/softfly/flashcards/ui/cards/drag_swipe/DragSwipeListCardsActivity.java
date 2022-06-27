package pl.softfly.flashcards.ui.cards.drag_swipe;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import pl.softfly.flashcards.ui.cards.standard.CardBaseViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.ListCardsActivity;

public class DragSwipeListCardsActivity extends ListCardsActivity {

    private boolean dragSwipeEnabled = true;

    private ItemTouchHelper itemTouchHelper;

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void onCreateRecyclerView() {
        super.onCreateRecyclerView();
        itemTouchHelper = new ItemTouchHelper(onCreateTouchHelper());
        getAdapter().setTouchHelper(itemTouchHelper);
        setDragSwipeEnabled(dragSwipeEnabled);
    }

    @Override
    protected CardBaseViewAdapter onCreateRecyclerViewAdapter() {
        return new DragSwipeCardBaseViewAdapter(this, getDeckDbPath());
    }

    @NonNull
    protected DragSwipeCardTouchHelper onCreateTouchHelper() {
        return new DragSwipeCardTouchHelper(getAdapter());
    }

    /* -----------------------------------------------------------------------------------------
     * Drag/swipe features
     * ----------------------------------------------------------------------------------------- */

    protected void setDragSwipeEnabled(boolean dragSwipeEnabled) {
        if (itemTouchHelper != null) {
            if (dragSwipeEnabled) {
                itemTouchHelper.attachToRecyclerView(getRecyclerView());
            } else {
                itemTouchHelper.attachToRecyclerView(null);
            }
        }
        this.dragSwipeEnabled = dragSwipeEnabled;
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected DragSwipeCardBaseViewAdapter getAdapter() {
        return (DragSwipeCardBaseViewAdapter) super.getAdapter();
    }
}