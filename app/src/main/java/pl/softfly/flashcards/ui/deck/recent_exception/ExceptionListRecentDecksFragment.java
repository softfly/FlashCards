package pl.softfly.flashcards.ui.deck.recent_exception;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.deck.folder.FolderDeckBaseViewAdapter;
import pl.softfly.flashcards.ui.deck.folder_exception.ExceptionFolderDeckBaseViewAdapter;
import pl.softfly.flashcards.ui.deck.recent.ListRecentDecksFragment;
import pl.softfly.flashcards.ui.deck.recent.RecentDeckBaseViewAdapter;
import pl.softfly.flashcards.ui.deck.standard.ListDecksFragment;
import pl.softfly.flashcards.ui.main.MainActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionListRecentDecksFragment extends ListRecentDecksFragment {

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getExceptionHandler().tryRun(
                () -> super.onCreate(savedInstanceState),
                getParentFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while creating fragment."
        );
    }

    @Override
    protected ExceptionRecentDeckBaseViewAdapter onCreateAdapter() {
        return new ExceptionRecentDeckBaseViewAdapter((MainActivity) getActivity());
    }

    /* -----------------------------------------------------------------------------------------
     * Fragment methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onResume() {
        getExceptionHandler().tryRun(
                () -> super.onResume(),
                getParentFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while resuming fragment."
        );
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        getExceptionHandler().tryRun(
                () -> super.onCreateOptionsMenu(menu, inflater),
                getParentFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while creating toolbar menu."
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            getExceptionHandler().handleException(
                    e, getParentFragmentManager(),
                    this.getClass().getSimpleName(),
                    "Error while selecting item in the toolbar menu."
            );
            return false;
        }
    }
}