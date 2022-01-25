package pl.softfly.flashcards.ui.cards.drag_swipe;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class DragSwipeCardViewHolder extends CardViewHolder {

    protected DragSwipeCardRecyclerViewAdapter adapter;

    public DragSwipeCardViewHolder(@NonNull View itemView, DragSwipeCardRecyclerViewAdapter adapter) {
        super(itemView, adapter);
        this.adapter = adapter;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        adapter.getTouchHelper().startDrag(this);
    }
}