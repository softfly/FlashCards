package pl.softfly.flashcards.ui.cards.standard;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    protected final static int MOVE_POPUP_SLIGHTLY_TO_LEFT = 200;
    protected final static int MOVE_POPUP_SLIGHTLY_TO_UNDER = 100;

    private final CardRecyclerViewAdapter adapter;
    private final TextView idTextView;
    private final TextView termTextView;
    private final TextView definitionTextView;
    @NonNull
    private final GestureDetector gestureDetector;
    private float lastTouchX;
    private float lastTouchY;

    public CardViewHolder(@NonNull View itemView, CardRecyclerViewAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        idTextView = itemView.findViewById(R.id.id);
        termTextView = itemView.findViewById(R.id.term);
        definitionTextView = itemView.findViewById(R.id.definition);
        gestureDetector = new GestureDetector(itemView.getContext(), this);
        itemView.setOnTouchListener(this);
    }

    @SuppressLint("RestrictedApi")
    public void showPopupMenu() {
        // A view that allows to display a popup with coordinates.
        final ViewGroup layout = adapter.getActivity().findViewById(R.id.listCards);
        final View view = createParentViewPopupMenu();
        layout.addView(view);
        PopupMenu popupMenu = new PopupMenu(
                this.itemView.getContext(),
                view,
                Gravity.TOP | Gravity.LEFT
        );
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_card, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this::onPopupMenuItemClick);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popupMenu.setForceShowIcon(true);
        popupMenu.setOnDismissListener(menu -> {
            layout.removeView(view);
        });
        popupMenu.show();
    }

    /**
     * A view that allows to display a popup with coordinates.
     */
    @NonNull
    protected View createParentViewPopupMenu() {
        final View view = new View(adapter.getActivity());
        view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        view.setX(getLastTouchX() - MOVE_POPUP_SLIGHTLY_TO_LEFT);
        final float maxY_ToDisplay = itemView.getY() + itemView.getHeight();
        view.setY(Math.min(getLastTouchY() + MOVE_POPUP_SLIGHTLY_TO_UNDER, maxY_ToDisplay));
        return view;
    }

    protected boolean onPopupMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit: {
                adapter.startEditCardActivity(getBindingAdapterPosition());
                return true;
            }
            case R.id.add: {
                adapter.startNewCardActivity(getBindingAdapterPosition());
                return true;
            }
            case R.id.delete: {
                adapter.onClickDeleteCard(getBindingAdapterPosition());
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
    public boolean onTouch(View v, @NonNull MotionEvent event) {
        lastTouchX = event.getX();
        lastTouchY = event.getY();
        gestureDetector.onTouchEvent(event);
        return false;
    }

    protected TextView getIdTextView() {
        return idTextView;
    }

    protected TextView getTermTextView() {
        return termTextView;
    }

    protected TextView getDefinitionTextView() {
        return definitionTextView;
    }

    protected float getLastTouchX() {
        return lastTouchX;
    }

    protected float getLastTouchY() {
        return lastTouchY;
    }
}
