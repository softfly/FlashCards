package pl.softfly.flashcards.filesync.db;

import android.content.Context;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Grzegorz Ziemski
 */
public class SyncDatabaseUtil {

    private static SyncDatabaseUtil INSTANCE;
    private final Map<String, SyncDeckDatabase> DECKS = new WeakHashMap<>();
    private SyncDeckDatabaseUtil syncDeckDatabaseUtil;

    protected SyncDatabaseUtil(Context context) {
        this.syncDeckDatabaseUtil = new SyncDeckDatabaseUtil(context);
    }

    public static synchronized SyncDatabaseUtil getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SyncDatabaseUtil(context);
        }
        return INSTANCE;
    }

    public synchronized SyncDeckDatabase getDeckDatabase(String dbName) {
        SyncDeckDatabase db = DECKS.get(dbName);
        if (db == null) {
            db = syncDeckDatabaseUtil.getDatabase(dbName);
            DECKS.put(dbName, db);
        } else if (!db.isOpen()) {
            db = syncDeckDatabaseUtil.getDatabase(dbName);
            DECKS.put(dbName, db);
        }
        return db;
    }

    public synchronized void closeDeckDatabase(String dbName) {
        SyncDeckDatabase db = DECKS.get(dbName);
        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
            DECKS.remove(dbName);
        }
    }

    public SyncDeckDatabaseUtil getSyncDeckDatabaseUtil() {
        return syncDeckDatabaseUtil;
    }
}
