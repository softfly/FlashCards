package pl.softfly.flashcards.filesync.db;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import pl.softfly.flashcards.db.AppStorageDbUtil;
import pl.softfly.flashcards.db.ExternalStorageDbUtil;

/**
 * @author Grzegorz Ziemski
 */
public class SyncDeckDatabaseUtil {

    private final static boolean EXTERNAL_STORAGE = true;

    private AppStorageDbUtil<SyncDeckDatabase> deckDbUtil;

    private Context context;

    public SyncDeckDatabaseUtil(Context context) {
        this.context = context;
        this.deckDbUtil = EXTERNAL_STORAGE ? new ExternalStorageDbUtil(context) {
            @Override
            protected Class getTClass() {
                return SyncDeckDatabase.class;
            }
        } : new AppStorageDbUtil(context) {
            @Override
            protected Class getTClass() {
                return SyncDeckDatabase.class;
            }
        };
    }

    public synchronized SyncDeckDatabase getDatabase(@NonNull String deckName) {
        return deckDbUtil.getDatabase(deckName);
    }

    public List<String> listDatabases() {
        return deckDbUtil.listDatabases();
    }

    public boolean exists(@NonNull String deckName) {
        return deckDbUtil.exists(deckName);
    }

    public void removeDatabase(@NonNull String deckName) {
        SyncDatabaseUtil.getInstance(context).closeDeckDatabase(deckName);
        deckDbUtil.removeDatabase(deckName);
    }

}