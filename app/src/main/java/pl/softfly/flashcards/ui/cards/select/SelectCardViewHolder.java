package pl.softfly.flashcards.ui.cards.select;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;

import com.google.android.material.color.MaterialColors;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.ItemCardBinding;
import pl.softfly.flashcards.ui.cards.drag_swipe.DragSwipeCardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class SelectCardViewHolder extends DragSwipeCardViewHolder {

    public SelectCardViewHolder(ItemCardBinding binding, SelectCardBaseViewAdapter adapter) {
        super(binding, adapter);
    }

    /* -----------------------------------------------------------------------------------------
     * C_02_04 When any card is selected and long pressing on the card, show the selected popup menu.
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void createSingleTapMenu(PopupMenu popupMenu) {
        super.createSingleTapMenu(popupMenu);
        popupMenu.getMenu().findItem(R.id.select).setVisible(!getAdapter().isSelectionMode());
    }

    /**
     * C_02_04 When any card is selected and long pressing on the card, show the selected popup menu.
     */
    public void showSelectPopupMenu() {
        showPopupMenu(this::createSelectSingleTapMenu);
    }

    protected void createSelectSingleTapMenu(PopupMenu popupMenu) {
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_card_select_mode, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this::onPopupMenuItemClick);
        showSelectOrUnselect(popupMenu);
        showPaste(popupMenu);
    }

    @Override
    protected boolean onPopupMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select: {
                getAdapter().onCardSelect(this);
                return true;
            }
            case R.id.unselect: {
                getAdapter().onCardUnselect(this);
                return true;
            }
            case R.id.deselect_all: {
                getAdapter().onClickDeselectAll();
                return true;
            }
            case R.id.delete_selected: {
                getAdapter().onClickDeleteSelected();
                return true;
            }
            case R.id.paste_before: {
                getAdapter().onClickPasteCards(getBindingAdapterPosition());
                return true;
            }
            case R.id.paste:
            case R.id.paste_after: {
                getAdapter().onClickPasteCards(getBindingAdapterPosition() + 1);
                return true;
            }
        }
        return super.onPopupMenuItemClick(item);
    }

    private void showSelectOrUnselect(@NonNull PopupMenu popup) {
        if (getAdapter().isCardSelected(getBindingAdapterPosition())) {
            popup.getMenu().findItem(R.id.select).setVisible(false);
            popup.getMenu().findItem(R.id.unselect).setVisible(true);
        } else {
            popup.getMenu().findItem(R.id.select).setVisible(true);
            popup.getMenu().findItem(R.id.unselect).setVisible(false);
        }
    }

    /**
     * Show "Paste" for the first item, otherwise show "Paste Before", "Paste After".
     */
    private void showPaste(@NonNull PopupMenu popup) {
        if (getBindingAdapterPosition() == 0) {
            popup.getMenu().findItem(R.id.paste).setVisible(false);
            popup.getMenu().findItem(R.id.paste_after).setVisible(true);
            popup.getMenu().findItem(R.id.paste_before).setVisible(true);
        } else {
            popup.getMenu().findItem(R.id.paste).setVisible(true);
            popup.getMenu().findItem(R.id.paste_after).setVisible(false);
            popup.getMenu().findItem(R.id.paste_before).setVisible(false);
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Implementation of GestureDetector
     * ----------------------------------------------------------------------------------------- */

    /**
     * C_02_03 When any card is selected and tap on the card, select or unselect the card.
     */
    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        if (getAdapter().isSelectionMode()) {
            getAdapter().onCardInvertSelect(this);
            return false;
        } else {
            return super.onSingleTapUp(e);
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Change the look of the view.
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void unfocusItemView() {
        // Check that the card has not been previously selected.
        int position = getBindingAdapterPosition();
        if (-1 < position && position < getAdapter().getItemCount()) {
            if (getAdapter().isCardSelected(position)) {
                selectItemView();
            } else {
                super.unfocusItemView();
            }
        }
    }

    protected void selectItemView() {
        this.itemView.setSelected(true);
        this.itemView.setBackgroundColor(
                MaterialColors.getColor(this.itemView, R.attr.colorItemSelected)
        );
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    public SelectCardBaseViewAdapter getAdapter() {
        return (SelectCardBaseViewAdapter) super.getAdapter();
    }
}
