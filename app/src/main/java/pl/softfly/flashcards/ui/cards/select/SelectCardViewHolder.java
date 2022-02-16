package pl.softfly.flashcards.ui.cards.select;

import android.graphics.Color;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.cards.drag_swipe.DragSwipeCardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class SelectCardViewHolder extends DragSwipeCardViewHolder {

    private final SelectCardRecyclerViewAdapter adapter;

    public SelectCardViewHolder(@NonNull View itemView, SelectCardRecyclerViewAdapter adapter) {
        super(itemView, adapter);
        this.adapter = adapter;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        adapter.onCardInvertSelect(this);
        return false;
    }

    @Override
    protected PopupMenu initPopupMenu() {
        PopupMenu popup = super.initPopupMenu();
        popup.setOnDismissListener(this::unfocusCard);
        popup.getMenu().findItem(R.id.select).setVisible(!adapter.isSelectionMode());
        return popup;
    }

    public void showSelectPopupMenu() {
        PopupMenu popup = new PopupMenu(
                this.itemView.getContext(),
                this.itemView,
                Gravity.START,
                0,
                R.style.PopupMenuWithLeftOffset
        );
        popup.getMenuInflater().inflate(R.menu.popup_menu_card_select_mode, popup.getMenu());
        popup.setOnMenuItemClickListener(this::onPopupMenuItemClick);
        popup.setOnDismissListener(this::unfocusCard);
        showSelect(popup);
        showPaste(popup);
        popup.show();
    }

    @Override
    protected boolean onPopupMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select: {
                adapter.onCardSelect(this);
                return true;
            }
            case R.id.unselect: {
                adapter.onCardUnselect(this);
                return true;
            }
            case R.id.deselect_all: {
                adapter.onClickDeselectAll();
                return true;
            }
            case R.id.delete_selected: {
                adapter.onClickDeleteSelected();
                return true;
            }
            case R.id.paste_before: {
                adapter.onClickPasteCards(getAdapterPosition());
                return true;
            }
            case R.id.paste:
            case R.id.paste_after: {
                adapter.onClickPasteCards(getAdapterPosition() + 1);
                return true;
            }
        }
        return super.onPopupMenuItemClick(item);
    }

    private void showSelect(@NonNull PopupMenu popup) {
        if (adapter.isCardSelected(getAdapterPosition())) {
            popup.getMenu().findItem(R.id.select).setVisible(false);
            popup.getMenu().findItem(R.id.unselect).setVisible(true);
        } else {
            popup.getMenu().findItem(R.id.select).setVisible(true);
            popup.getMenu().findItem(R.id.unselect).setVisible(false);
        }
    }

    private void showPaste(@NonNull PopupMenu popup) {
        if (getAdapterPosition() == 0) {
            popup.getMenu().findItem(R.id.paste).setVisible(false);
            popup.getMenu().findItem(R.id.paste_after).setVisible(true);
            popup.getMenu().findItem(R.id.paste_before).setVisible(true);
        } else {
            popup.getMenu().findItem(R.id.paste).setVisible(true);
            popup.getMenu().findItem(R.id.paste_after).setVisible(false);
            popup.getMenu().findItem(R.id.paste_before).setVisible(false);
        }
    }

    private void unfocusCard(PopupMenu p) {
        // Check that the card has not been removed.
        int position = getBindingAdapterPosition();
        if (-1 < position && position < adapter.getItemCount()){
            if (adapter.isCardSelected(getBindingAdapterPosition())) {
                this.itemView.setBackgroundColor(Color.LTGRAY);
            } else {
                this.itemView.setBackgroundColor(0);
            }
        }
    }
}
