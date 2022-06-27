package pl.softfly.flashcards.ui.cards.exception;

import android.view.MenuItem;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.databinding.ItemCardBinding;
import pl.softfly.flashcards.ui.cards.file_sync.FileSyncCardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionCardViewHolder extends FileSyncCardViewHolder {

    public ExceptionCardViewHolder(
            ItemCardBinding binding,
            ExceptionCardBaseViewAdapter adapter
    ) {
        super(binding, adapter);
    }

    /* -----------------------------------------------------------------------------------------
     * C_02_01 When no card is selected and tap on the card, show the popup menu.
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected boolean onPopupMenuItemClick(@NonNull MenuItem item) {
        try {
            return super.onPopupMenuItemClick(item);
        } catch (Exception e) {
            getExceptionHandler().handleException(
                    e, getAdapter().getActivity().getSupportFragmentManager(),
                    this.getClass().getSimpleName() + "_OnPopupMenuItemClick"
            );
            return false;
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Implementation of GestureDetector
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onLongPress(MotionEvent e) {
        getExceptionHandler().tryRun(
                () -> super.onLongPress(e),
                getAdapter().getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_OnLongPress"
        );
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent event) {
        try {
            return super.onSingleTapUp(event);
        } catch (Exception e) {
            getExceptionHandler().handleException(
                    e, getAdapter().getActivity().getSupportFragmentManager(),
                    this.getClass().getSimpleName() + "_OnSingleTapUp"
            );
            return false;
        }
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}