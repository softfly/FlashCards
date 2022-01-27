package pl.softfly.flashcards.ui.cards.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.ui.cards.file_sync.FileSyncCardTouchHelper;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionCardTouchHelper extends FileSyncCardTouchHelper {

    private final ExceptionHandler exceptionHandler = new ExceptionHandler();
    private final ExceptionCardRecyclerViewAdapter adapter;

    public ExceptionCardTouchHelper(ExceptionCardRecyclerViewAdapter adapter) {
        super(adapter);
        this.adapter = adapter;
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        exceptionHandler.tryHandleException(
                () -> super.clearView(recyclerView, viewHolder),
                adapter.getActivity().getSupportFragmentManager(),
                ExceptionCardTouchHelper.class.getSimpleName() + "_ClearView"
        );
    }

    @Override
    public void onSelectedChanged(
            @Nullable RecyclerView.ViewHolder viewHolder,
            int actionState
    ) {
        exceptionHandler.tryHandleException(
                () -> super.onSelectedChanged(viewHolder, actionState),
                adapter.getActivity().getSupportFragmentManager(),
                ExceptionCardTouchHelper.class.getSimpleName() + "_OnSelectedChanged"
        );
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        try {
            return super.onMove(recyclerView, viewHolder, target);
        } catch (Exception e) {
            exceptionHandler.handleException(
                    e, adapter.getActivity().getSupportFragmentManager(),
                    ExceptionCardTouchHelper.class.getSimpleName() + "_OnMove"
            );
            return false;
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        exceptionHandler.tryHandleException(
                () -> super.onSwiped(viewHolder, direction),
                adapter.getActivity().getSupportFragmentManager(),
                ExceptionCardTouchHelper.class.getSimpleName() + "_OnSwiped"
        );
    }
}
