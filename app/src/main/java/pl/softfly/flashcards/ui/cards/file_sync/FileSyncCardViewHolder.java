package pl.softfly.flashcards.ui.cards.file_sync;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;

import com.google.android.material.color.MaterialColors;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.cards.select.SelectCardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncCardViewHolder extends SelectCardViewHolder {

    protected FileSyncCardRecyclerViewAdapter adapter;

    public FileSyncCardViewHolder(
            @NonNull View itemView,
            FileSyncCardRecyclerViewAdapter adapter
    ) {
        super(itemView, adapter);
        this.adapter = adapter;
    }

    @Override
    public void showPopupMenu() {
        if (adapter.getActivity().isEditingUnlocked()) super.showPopupMenu();
    }

    @Override
    public void showSelectPopupMenu() {
        if (adapter.getActivity().isEditingUnlocked()) super.showSelectPopupMenu();
    }

    @Override
    protected boolean onPopupMenuItemClick(@NonNull MenuItem item) {
        if (adapter.getActivity().isEditingUnlocked()) return super.onPopupMenuItemClick(item);
        else throw new RuntimeException("Deck editing is locked. Sync in progress..");
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (adapter.getActivity().isEditingUnlocked()) super.onLongPress(e);
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        if (adapter.getActivity().isEditingUnlocked()) return super.onSingleTapUp(e);
        return false;
    }

    @Override
    protected void unfocusCard() {
        this.itemView.setActivated(false);
        // Check that the card has not been previously selected.
        int position = getBindingAdapterPosition();
        if (-1 < position && position < adapter.getItemCount()) {
            if (adapter.isCardSelected(getBindingAdapterPosition())) {
                selectItemView();
            } else if (adapter.isShowedRecentlySynced(getBindingAdapterPosition())) {
                adapter.setRecentlySyncedBackground(this, position);
            } else {
                unselectItemView();
            }
        }
    }
}