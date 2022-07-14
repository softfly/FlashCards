package pl.softfly.flashcards.ui.deck.folder_exception;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.ui.deck.folder.FolderViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionFolderViewHolder extends FolderViewHolder {

    public ExceptionFolderViewHolder(
            @NonNull View itemView,
            ExceptionFolderDeckViewAdapter adapter
    ) {
        super(itemView, adapter);
    }

    @Override
    protected boolean onMenuMoreClick(MenuItem item) {
        try {
            return super.onMenuMoreClick(item);
        } catch (Exception e) {
            getExceptionHandler().handleException(
                    e, getAdapter().getActivity().getSupportFragmentManager(),
                    this.getClass().getSimpleName(),
                    "Error clicking on item in the more menu."
            );
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        getExceptionHandler().tryRun(
                () -> super.onClick(view),
                getAdapter().getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_OnClick",
                "Error clicking on item."
        );
    }

}