package pl.softfly.flashcards.filesync.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import pl.softfly.flashcards.Config;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.db.storage.AppStorageDb;
import pl.softfly.flashcards.db.storage.ExternalStorageDb;
import pl.softfly.flashcards.db.storage.StorageDb;

/**
 * Service locator to maintain and caching only one connection per database.
 * https://developer.android.com/training/dependency-injection#di-alternatives
 *
 * @author Grzegorz Ziemski
 */
public class FileSyncDatabaseUtil {

    private static FileSyncDatabaseUtil INSTANCE;

    private final Map<String, FileSyncDeckDatabase> DECKS = new WeakHashMap<>();

    @NonNull
    private final StorageDb<FileSyncDeckDatabase> storageDb;

    protected FileSyncDatabaseUtil(@NonNull Context appContext) {
        this.storageDb = Config.getInstance(appContext).isDatabaseExternalStorage() ?
                new ExternalStorageDb<FileSyncDeckDatabase>(appContext) {
                    @NonNull
                    @Override
                    protected Class<FileSyncDeckDatabase> getTClass() {
                        return FileSyncDeckDatabase.class;
                    }
                } :
                new AppStorageDb<FileSyncDeckDatabase>(appContext) {
                    @NonNull
                    @Override
                    protected Class<FileSyncDeckDatabase> getTClass() {
                        return FileSyncDeckDatabase.class;
                    }
                };
    }

    public static synchronized FileSyncDatabaseUtil getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new FileSyncDatabaseUtil(context);
        }
        return INSTANCE;
    }
    @NonNull
    public synchronized FileSyncDeckDatabase getDeckDatabase(@NonNull File folder, @NonNull String name) {
        return getDeckDatabase(folder.getPath(), name);
    }

    @NonNull
    public synchronized FileSyncDeckDatabase getDeckDatabase(@NonNull String folder, @NonNull String name) {
        return getDeckDatabase(folder + "/" + name);
    }

    @NonNull
    public synchronized FileSyncDeckDatabase getDeckDatabase(@NonNull String dbPath) {
        Objects.nonNull(dbPath);
        FileSyncDeckDatabase db = DECKS.get(dbPath);
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
        FileSyncDeckDatabase db = DECKS.get(dbName);
        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
            DECKS.remove(dbName);
        }
    }

    @NonNull
    public StorageDb<FileSyncDeckDatabase> getStorageDb() {
        return storageDb;
    }
}
