package pl.softfly.flashcards.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

import pl.softfly.flashcards.Config;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.db.storage.AppStorageDb;
import pl.softfly.flashcards.db.storage.ExternalStorageDb;
import pl.softfly.flashcards.db.storage.StorageDb;

/**
 * Service locator to maintain and caching only one connection per database.
 * https://developer.android.com/training/dependency-injection#di-alternatives
 *
 * @author Grzegorz Ziemski
 */
public class AppDatabaseUtil {

    private static AppDatabaseUtil INSTANCE;

    private final Map<String, DeckDatabase> DECKS = new WeakHashMap<>();

    @NonNull
    private final StorageDb<DeckDatabase> storageDb;

    protected AppDatabaseUtil(@NonNull Context appContext) {
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

    @Nullable
    public synchronized DeckDatabase getDeckDatabase(@NonNull String dbName) {
        DeckDatabase db = DECKS.get(dbName);
        if (db == null) {
            db = storageDb.getDatabase(dbName);
            DECKS.put(dbName, db);
        } else if (!db.isOpen()) {
            db = storageDb.getDatabase(dbName);
            DECKS.put(dbName, db);
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

    @NonNull
    public StorageDb<DeckDatabase> getStorageDb() {
        return storageDb;
    }
}
