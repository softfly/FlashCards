package pl.softfly.flashcards.ui.base;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import io.reactivex.rxjava3.functions.Consumer;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;

/**
 * @author Grzegorz Ziemski
 */
public class BaseFragmentActivity extends FragmentActivity {

    public void runOnUiThread(Runnable action, Consumer<? super Throwable> onError) {
        getExceptionHandler().tryRun(() -> runOnUiThread(() -> action.run()), onError);
    }

    @Nullable
    protected DeckDatabase getDeckDatabase(String dbPath) {
        return DeckDatabaseUtil
                .getInstance(getApplicationContext())
                .getDatabase(dbPath);
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }

}
