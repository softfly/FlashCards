package pl.softfly.flashcards.ui.base;

import androidx.fragment.app.Fragment;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.DeckDatabaseUtil;

/**
 * @author Grzegorz Ziemski
 */
public class BaseFragment extends Fragment {

    protected DeckDatabaseUtil getDeckDatabaseUtil() {
        return DeckDatabaseUtil.getInstance(getContext());
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
