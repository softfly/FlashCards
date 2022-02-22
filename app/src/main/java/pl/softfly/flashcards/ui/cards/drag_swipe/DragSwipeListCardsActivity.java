package pl.softfly.flashcards.ui.cards.drag_swipe;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.ui.cards.standard.CardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.ListCardsActivity;

public class DragSwipeListCardsActivity extends ListCardsActivity {

    private DragSwipeCardRecyclerViewAdapter adapter;

    @Override
    protected void initRecyclerView() {
        super.initRecyclerView();
        RecyclerView recyclerView = getRecyclerView();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(onCreateTouchHelper());
        itemTouchHelper.attachToRecyclerView(recyclerView);
        adapter.setTouchHelper(itemTouchHelper);
    }

    @Override
    protected CardRecyclerViewAdapter onCreateRecyclerViewAdapter() {
        this.adapter = new DragSwipeCardRecyclerViewAdapter(this, getDeckName());
        setAdapter(adapter);
        return adapter;
    }

    @NonNull
    protected ItemTouchHelper.Callback onCreateTouchHelper() {
        return new DragSwipeCardTouchHelper(adapter);
    }

    protected void setAdapter(DragSwipeCardRecyclerViewAdapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
    }
}