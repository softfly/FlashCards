package pl.softfly.flashcards.ui.cards.file_sync;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.ui.cards.select.SelectCardTouchHelper;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncCardTouchHelper extends SelectCardTouchHelper {

    FileSyncCardRecyclerViewAdapter adapter;

    public FileSyncCardTouchHelper(FileSyncCardRecyclerViewAdapter adapter) {
        super(adapter);
        this.adapter = adapter;
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        if (adapter.getActivity().isEditingUnlocked()) {
            super.clearView(recyclerView, viewHolder);
        }
    }

    @Override
    public void onSelectedChanged(
            @Nullable RecyclerView.ViewHolder viewHolder,
            int actionState
    ) {
        if (adapter.getActivity().isEditingUnlocked()) {
            super.onSelectedChanged(viewHolder, actionState);
        }
    }
}
