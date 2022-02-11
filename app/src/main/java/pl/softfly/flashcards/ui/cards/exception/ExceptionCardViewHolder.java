package pl.softfly.flashcards.ui.cards.exception;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.ui.cards.file_sync.FileSyncCardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionCardViewHolder extends FileSyncCardViewHolder {

    private final ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();

    public ExceptionCardViewHolder(
            @NonNull View itemView,
            ExceptionCardRecyclerViewAdapter adapter
    ) {
        super(itemView, adapter);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        try {
            return super.onSingleTapUp(event);
        } catch (Exception e) {
            exceptionHandler.handleException(
                    e, adapter.getActivity().getSupportFragmentManager(),
                    ExceptionCardViewHolder.class.getSimpleName() + "_OnSingleTapUp"
            );
            return false;
        }
    }

    @Override
    protected boolean onPopupMenuItemClick(@NonNull MenuItem item) {
        try {
            return super.onPopupMenuItemClick(item);
        } catch (Exception e) {
            exceptionHandler.handleException(
                    e, adapter.getActivity().getSupportFragmentManager(),
                    ExceptionCardViewHolder.class.getSimpleName() + "_OnPopupMenuItemClick"
            );
            return false;
        }
    }
}