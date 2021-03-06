package pl.softfly.flashcards.ui.base;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.reactivex.rxjava3.functions.Consumer;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;

/**
 * @author Grzegorz Ziemski
 */
public class BaseFragment extends Fragment {

    public void runOnUiThread(Runnable action, Consumer<? super Throwable> onError) {
        getExceptionHandler().tryRun(() -> getActivity().runOnUiThread(() -> action.run()), onError);
    }

    @Nullable
    protected DeckDatabase getDeckDatabase(String dbPath) {
        return DeckDatabaseUtil
                .getInstance(getContext())
                .getDatabase(dbPath);
    }

    protected AppDatabase getAppDatabase() {
        return AppDatabaseUtil
                .getInstance(getContext())
                .getDatabase();
    }

    protected DeckDatabaseUtil getDeckDatabaseUtil() {
        return DeckDatabaseUtil.getInstance(getContext());
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
