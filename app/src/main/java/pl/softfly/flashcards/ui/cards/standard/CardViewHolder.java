package pl.softfly.flashcards.ui.cards.standard;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.R;

/**
 * @author Grzegorz Ziemski
 */
public class CardViewHolder extends RecyclerView.ViewHolder
        implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private final CardRecyclerViewAdapter adapter;
    private final TextView idTextView;
    private final TextView questionTextView;
    private final TextView answerTextView;
    @NonNull
    private final GestureDetector gestureDetector;

    public CardViewHolder(@NonNull View itemView, CardRecyclerViewAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        idTextView = itemView.findViewById(R.id.id);
        questionTextView = itemView.findViewById(R.id.question);
        answerTextView = itemView.findViewById(R.id.answer);
        gestureDetector = new GestureDetector(itemView.getContext(), this);
        itemView.setOnTouchListener(this);
    }

    public void showPopupMenu() {
        PopupMenu popup = initPopupMenu();
        popup.show();
    }

    protected PopupMenu initPopupMenu() {
        PopupMenu popup = new PopupMenu(
                this.itemView.getContext(),
                this.itemView,
                Gravity.START,
                0,
                R.style.PopupMenuWithLeftOffset
        );
        popup.getMenuInflater().inflate(R.menu.popup_menu_card, popup.getMenu());
        popup.setOnMenuItemClickListener(this::onPopupMenuItemClick);
        return popup;
    }

    protected boolean onPopupMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit: {
                adapter.startEditCardActivity(getAdapterPosition());
                return true;
            }
            case R.id.add: {
                adapter.startNewCardActivity(getAdapterPosition());
                return true;
            }
            case R.id.delete: {
                adapter.onClickDeleteCard(getAdapterPosition());
                return true;
            }
        }
        throw new RuntimeException(String.format("Not implemented itemId=%d", item.getItemId()));
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        showPopupMenu();
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return false;
    }

    protected TextView getIdTextView() {
        return idTextView;
    }

    protected TextView getQuestionTextView() {
        return questionTextView;
    }

    protected TextView getAnswerTextView() {
        return answerTextView;
    }
}
