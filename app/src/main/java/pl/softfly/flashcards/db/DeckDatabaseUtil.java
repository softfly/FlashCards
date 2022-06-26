package pl.softfly.flashcards.db;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
 * @author Grzegorz Ziemski
 */
public class DeckDatabaseUtil {

    private static DeckDatabaseUtil INSTANCE;

    private final Map<String, DeckDatabase> decks = new WeakHashMap<>();

    @NonNull
    private final StorageDb<DeckDatabase> storageDb;

    private final Context context;

    protected DeckDatabaseUtil(@NonNull Context context) {
        this.context = context;
        this.storageDb = Config.getInstance(context).isDatabaseExternalStorage() ?
                new ExternalStorageDb<DeckDatabase>(context) {
                    @NonNull
                    @Override
                    protected Class<DeckDatabase> getTClass() {
                        return DeckDatabase.class;
                    }
                } :
                new AppStorageDb<DeckDatabase>(context) {
                    @NonNull
                    @Override
                    protected Class<DeckDatabase> getTClass() {
                        return DeckDatabase.class;
                    }
                };
    }

    public static synchronized DeckDatabaseUtil getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DeckDatabaseUtil(context);
        }
        return INSTANCE;
    }

    @NonNull
    public synchronized DeckDatabase getDatabase(@NonNull File folder, @NonNull String name) {
        return getDatabase(folder.getPath(), name);
    }

    @NonNull
    public synchronized DeckDatabase getDatabase(@NonNull String folder, @NonNull String name) {
        return getDatabase(folder + "/" + name);
    }

    @NonNull
    public synchronized DeckDatabase getDatabase(@NonNull String path) {
        Objects.nonNull(path);
        if (!path.endsWith(".db")) {
            throw new RuntimeException("The database does not have the .db extension.");
        }

        DeckDatabase db = decks.get(path);
        if (db == null) {
            db = storageDb.getDatabase(path);
            decks.put(path, db);
        } else if (!db.isOpen()) {
            db = storageDb.getDatabase(path);
            decks.put(path, db);
        }
        return db;
    }

    @NonNull
    public synchronized DeckDatabase createDatabase(@NonNull String path) {
        Objects.nonNull(path);
        DeckDatabase db = storageDb.createDatabase(path);
        decks.put(path, db);
        return db;
    }

    public synchronized void closeDatabase(String dbName) {
        DeckDatabase db = decks.get(dbName);
        if (db != null && db.isOpen()) {
            decks.remove(dbName);
            Cursor c = db.query(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
            if (c.moveToFirst() && c.getInt(0) == 1)
                throw new RuntimeException("Checkpoint was blocked from completing");
            db.close();
        }
    }

    public Completable moveDatabase(String dbPath, String toFolderPath) {
        File from = new File(dbPath);
        File to = new File(toFolderPath + "/" + from.getName());
        return Completable.fromRunnable(() -> closeDatabase(dbPath))
                .andThen(Completable.fromRunnable(() -> {
                    if (from.renameTo(to)) {
                        (new File(from.getPath() + "-shm")).delete();
                        (new File(from.getPath() + "-wal")).delete();
                    } else {
                        throw new RuntimeException();
                    }
                }))
                .andThen(getAppDatabase().deckDaoAsync().updatePathByPath(to.getPath(), from.getPath()))
                .subscribeOn(Schedulers.io());
    }

    public Completable moveDatabase(String dbPath, String targetFolderPath, String replacementFolderPath) {
        String toPath = dbPath.replace(targetFolderPath, replacementFolderPath);
        File from = new File(dbPath);
        File to = new File(dbPath.replace(targetFolderPath, replacementFolderPath));
        return Completable.fromRunnable(() -> closeDatabase(dbPath))
                .andThen(Completable.fromRunnable(() -> {
                    if (from.renameTo(to)) {
                        (new File(from.getPath() + "-shm")).delete();
                        (new File(from.getPath() + "-wal")).delete();
                    } else {
                        throw new RuntimeException();
                    }
                }))
                .andThen(getAppDatabase().deckDaoAsync().updatePathByPath(to.getPath(), from.getPath()))
                .subscribeOn(Schedulers.io());
    }

    private String getDbName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public Completable removeDatabase(String path) {
        return Completable.fromRunnable(() -> storageDb.removeDatabase(path))
                .andThen(removeDeckFromAppDb(path));
    }

    protected Completable removeDeckFromAppDb(String path) {
        return getAppDatabase()
                .deckDaoAsync()
                .deleteByPath(path)
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    public StorageDb<DeckDatabase> getStorageDb() {
        return storageDb;
    }

    protected AppDatabase getAppDatabase() {
        return AppDatabaseUtil
                .getInstance(context)
                .getDatabase();
    }
}
