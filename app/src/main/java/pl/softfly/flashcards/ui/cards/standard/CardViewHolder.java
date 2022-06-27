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

import com.google.android.material.color.MaterialColors;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.ItemCardBinding;

/**
 * @author Grzegorz Ziemski
 */
public class CardViewHolder extends RecyclerView.ViewHolder
        implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private final CardBaseViewAdapter adapter;
    private final GestureDetector gestureDetector;
    private final ItemCardBinding binding;

    /**
     * Needed to define where the popup menu should be displayed.
     */
    private float lastTouchX;

    /**
     * Needed to define where the popup menu should be displayed.
     */
    private float lastTouchY;

    public CardViewHolder(ItemCardBinding binding, CardBaseViewAdapter adapter) {
        super(binding.getRoot());
        this.adapter = adapter;
        this.binding = binding;
        gestureDetector = new GestureDetector(itemView.getContext(), this);
        itemView.setOnTouchListener(this);
    }

    /* -----------------------------------------------------------------------------------------
     * C_02_01 When no card is selected and tap on the card, show the popup menu.
     * ----------------------------------------------------------------------------------------- */

    protected interface CreatePopupMenu {
        void create(PopupMenu popupMenu);
    }

    /**
     * Show popup at last tap location.
     */
    @SuppressLint("RestrictedApi")
    public void showPopupMenu(CreatePopupMenu createPopupMenu) {
        // A view that allows to display a popup with coordinates.
        final ViewGroup layout = adapter.getActivity().getListCardsView();
        final View view = createParentViewPopupMenu();
        layout.addView(view);

        PopupMenu popupMenu = new PopupMenu(
                itemView.getContext(),
                view,
                Gravity.TOP | Gravity.LEFT
        );
        createPopupMenu.create(popupMenu);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popupMenu.setForceShowIcon(true);
        popupMenu.setOnDismissListener(menu -> {
            layout.removeView(view);
            unfocusItemView();
        });
        popupMenu.show();
    }

    /**
     * C_02_01 When no card is selected and tap on the card, show the popup menu.
     */
    protected void createSingleTapMenu(PopupMenu popupMenu) {
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_card, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(CardViewHolder.this::onPopupMenuItemClick);
    }

    /**
     * A view that allows to display a popup with coordinates.
     */
    @NonNull
    protected View createParentViewPopupMenu() {
        final View view = new View(adapter.getActivity());
        view.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        view.setX(getLastTouchX());

        final float maxY = itemView.getY() + itemView.getHeight();
        view.setY(Math.min(getLastTouchY(), maxY));
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

    /* -----------------------------------------------------------------------------------------
     * Implementation of GestureDetector
     * ----------------------------------------------------------------------------------------- */

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    /**
     * C_02_01 When no card is selected and tap on the card, show the popup menu.
     */
    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent event) {
        lastTouchX = event.getRawX();
        lastTouchY = event.getRawY();
        focusSingleTapItemView();
        showPopupMenu(this::createSingleTapMenu);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false; // It might be used when ItemTouchHelper is not attached.
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // Do nothing
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, @NonNull MotionEvent event) {
        lastTouchX = event.getRawX();
        lastTouchY = event.getRawY();
        gestureDetector.onTouchEvent(event);
        return false;
    }

    /* -----------------------------------------------------------------------------------------
     * Change the look of the view.
     * ----------------------------------------------------------------------------------------- */

    public void focusSingleTapItemView() {
        itemView.setActivated(true);
        itemView.setBackgroundColor(
                MaterialColors.getColor(itemView, R.attr.colorItemActive)
        );
    }

    public void unfocusItemView() {
        this.itemView.setSelected(false);
        this.itemView.setBackgroundColor(0);
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    protected TextView getIdTextView() {
        return binding.id;
    }

    protected TextView getTermTextView() {
        return binding.term;
    }

    protected TextView getDefinitionTextView() {
        return binding.definition;
    }

    private float getLastTouchX() {
        return lastTouchX;
    }

    private float getLastTouchY() {
        return lastTouchY;
    }

    protected CardBaseViewAdapter getAdapter() {
        return adapter;
    }
}
