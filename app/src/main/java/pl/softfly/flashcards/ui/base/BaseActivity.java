package pl.softfly.flashcards.ui.base;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.functions.Consumer;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;

/**
 * @author Grzegorz Ziemski
 */
public class BaseActivity extends AppCompatActivity {

    public void runOnUiThread(Runnable action, Consumer<? super Throwable> onError) {
        getExceptionHandler().tryRun(() -> runOnUiThread(() -> action.run()), onError);
    }

    @Nullable
    protected DeckDatabase getDeckDatabase(String dbPath) {
        return DeckDatabaseUtil
                .getInstance(getApplicationContext())
                .getDatabase(dbPath);
    }

    protected AppDatabase getAppDatabase() {
        return AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getDatabase();
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
