package pl.softfly.flashcards.ui.cards.select;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;

import com.google.android.material.color.MaterialColors;

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

    /**
     * C_02_03 When any card is selected and tap on the card, select or unselect the card.
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (adapter.isSelectionMode()) {
            adapter.onCardInvertSelect(this);
            return false;
        } else {
            return super.onSingleTapUp(e);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
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
            unfocusCard(popupMenu);
        });
        popupMenu.getMenu().findItem(R.id.select).setVisible(!adapter.isSelectionMode());
        popupMenu.show();
    }

    /**
     * C_02_04 When any card is selected and long pressing on the card, show the selected popup menu.
     */
    public void showSelectPopupMenu() {
        // A view that allows to display a popup with coordinates.
        final ViewGroup layout = adapter.getActivity().findViewById(R.id.listCards);
        final View view = createParentViewPopupMenu();
        layout.addView(view);

        PopupMenu popupMenu = new PopupMenu(
                this.itemView.getContext(),
                view,
                Gravity.TOP | Gravity.LEFT
        );
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_card_select_mode, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this::onPopupMenuItemClick);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popupMenu.setForceShowIcon(true);
        popupMenu.setOnDismissListener(menu -> {
            layout.removeView(view);
            unfocusCard(popupMenu);
        });
        showSelect(popupMenu);
        showPaste(popupMenu);
        popupMenu.show();
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
        this.itemView.setActivated(false);
        // Check that the card has not been previously selected.
        int position = getBindingAdapterPosition();
        if (-1 < position && position < adapter.getItemCount()) {
            if (adapter.isCardSelected(getBindingAdapterPosition())) {
                this.itemView.setSelected(true);
                this.itemView.setBackgroundColor(
                        MaterialColors.getColor(this.itemView, R.attr.colorItemSelected)
                );

            } else {
                this.itemView.setSelected(false);
                this.itemView.setBackgroundColor(0);
            }
        }
    }
}
