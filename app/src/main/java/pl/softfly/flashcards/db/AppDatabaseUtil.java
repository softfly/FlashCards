package pl.softfly.flashcards.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import pl.softfly.flashcards.Config;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.db.storage.AppStorageDb;
import pl.softfly.flashcards.db.storage.ExternalStorageDb;
import pl.softfly.flashcards.db.storage.StorageDb;

/**
 * Service locator to maintain and caching only one connection per database.
 * https://developer.android.com/training/dependency-injection#di-alternatives
 *
 * TODO Split into AppDatabaseUtil and DeckDatabaseUtil
 *
 * @author Grzegorz Ziemski
 */
public class AppDatabaseUtil {

    private static AppDatabaseUtil INSTANCE;

    private final Map<String, DeckDatabase> DECKS = new WeakHashMap<>();

    @NonNull
    private final StorageDb<DeckDatabase> storageDb;

    private final Context appContext;

    protected AppDatabaseUtil(@NonNull Context appContext) {
        this.appContext = appContext;
        this.storageDb = Config.getInstance(appContext).isDatabaseExternalStorage() ?
                new ExternalStorageDb<DeckDatabase>(appContext) {
                    @NonNull
                    @Override
                    protected Class<DeckDatabase> getTClass() {
                        return DeckDatabase.class;
                    }
                } :
                new AppStorageDb<DeckDatabase>(appContext) {
                    @NonNull
                    @Override
                    protected Class<DeckDatabase> getTClass() {
                        return DeckDatabase.class;
                    }
                };
    }

    public static synchronized AppDatabaseUtil getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppDatabaseUtil(context);
        }
        return INSTANCE;
    }

    @NonNull
    public synchronized DeckDatabase getDeckDatabase(@NonNull File folder, @NonNull String name) {
        return getDeckDatabase(folder.getPath(), name);
    }

    @NonNull
    public synchronized DeckDatabase getDeckDatabase(@NonNull String folder, @NonNull String name) {
        return getDeckDatabase(folder + "/" + name);
    }

    @NonNull
    public synchronized DeckDatabase getDeckDatabase(@NonNull String dbPath) {
        Objects.nonNull(dbPath);
        DeckDatabase db = DECKS.get(dbPath);
        if (db == null) {
            db = storageDb.getDatabase(dbPath);
            DECKS.put(dbPath, db);
        } else if (!db.isOpen()) {
            db = storageDb.getDatabase(dbPath);
            DECKS.put(dbPath, db);
        }
        return db;
    }

    public synchronized void closeDeckDatabase(String dbName) {
        DeckDatabase db = DECKS.get(dbName);
        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
            DECKS.remove(dbName);
        }
    }

    //TODO rename to DeckStorageDB
    @NonNull
    public StorageDb<DeckDatabase> getStorageDb() {
        return storageDb;
    }

    @NonNull
    public AppDatabase getAppDatabase() {
        return Room.databaseBuilder(
                appContext,
                AppDatabase.class,
                "AppCore"
        ).build();
    }
}
