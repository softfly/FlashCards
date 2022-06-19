package pl.softfly.flashcards.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import java.lang.ref.WeakReference;

import pl.softfly.flashcards.db.room.AppDatabase;

/**
 * Service locator to maintain and caching only one connection per database.
 * https://developer.android.com/training/dependency-injection#di-alternatives
 *
 * @author Grzegorz Ziemski
 */
public class AppDatabaseUtil {

    private static WeakReference<AppDatabaseUtil> INSTANCE;

    private final Context appContext;

    private WeakReference<AppDatabase> appDatabase;

    protected AppDatabaseUtil(Context appContext) {
        this.appContext = appContext;
    }

    public static synchronized AppDatabaseUtil getInstance(@NonNull Context context) {
        if (INSTANCE == null || INSTANCE.get() == null) {
            INSTANCE = new WeakReference<>(new AppDatabaseUtil(context));
        }
        return INSTANCE.get();
    }

    public AppDatabase getDatabase() {
        if (appDatabase == null || appDatabase.get() == null) {
            appDatabase = new WeakReference<>(Room.databaseBuilder(
                    appContext,
                    AppDatabase.class,
                    "App"
            ).build());
        }
        return appDatabase.get();
    }
}
