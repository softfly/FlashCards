package pl.softfly.flashcards.ui.cards.drag_swipe;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import pl.softfly.flashcards.ui.cards.standard.CardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.ListCardsActivity;

public class DragSwipeListCardsActivity extends ListCardsActivity {

    private DragSwipeCardRecyclerViewAdapter adapter;

    private boolean dragSwipeEnabled = true;

    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreateRecyclerView() {
        super.onCreateRecyclerView();
        itemTouchHelper = new ItemTouchHelper(onCreateTouchHelper());
        setDragSwipeEnabled(dragSwipeEnabled);
        adapter.setTouchHelper(itemTouchHelper);
    }

    @Override
    protected CardRecyclerViewAdapter onCreateRecyclerViewAdapter() {
        adapter = new DragSwipeCardRecyclerViewAdapter(this, deckDbPath);
        setAdapter(adapter);
        return adapter;
    }

    @NonNull
    protected ItemTouchHelper.Callback onCreateTouchHelper() {
        return new DragSwipeCardTouchHelper(adapter);
    }

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

    protected void setAdapter(DragSwipeCardRecyclerViewAdapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
    }
}