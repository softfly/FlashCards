package pl.softfly.flashcards.ui.base;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;

/**
 * @author Grzegorz Ziemski
 */
public class BaseDialogFragment extends DialogFragment {

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }

    @Nullable
    protected AppDatabase getAppDatabase() {
        return AppDatabaseUtil
                .getInstance(getContext())
                .getDatabase();
    }

}
