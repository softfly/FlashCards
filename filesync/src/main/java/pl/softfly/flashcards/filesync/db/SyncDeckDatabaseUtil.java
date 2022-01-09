package pl.softfly.flashcards.filesync.db;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import pl.softfly.flashcards.db.AppStorageDbUtil;
import pl.softfly.flashcards.db.ExternalStorageDbUtil;

/**
 * @author Grzegorz Ziemski
 */
public class SyncDeckDatabaseUtil {

    private final static boolean EXTERNAL_STORAGE = true;

    @NonNull
    private final AppStorageDbUtil<SyncDeckDatabase> deckDbUtil;

    private final Context appContext;

    public SyncDeckDatabaseUtil(Context appContext) {
        this.appContext = appContext;
        this.deckDbUtil = EXTERNAL_STORAGE ?
                new ExternalStorageDbUtil<SyncDeckDatabase>(appContext) {
                    @NonNull
                    @Override
                    protected Class<SyncDeckDatabase> getTClass() {
                        return SyncDeckDatabase.class;
                    }
                } :
                new AppStorageDbUtil<SyncDeckDatabase>(appContext) {
                    @NonNull
                    @Override
                    protected Class<SyncDeckDatabase> getTClass() {
                        return SyncDeckDatabase.class;
                    }
                };
    }

    public synchronized SyncDeckDatabase getDatabase(@NonNull String deckName) {
        return deckDbUtil.getDatabase(deckName);
    }

    @NonNull
    public List<String> listDatabases() {
        return deckDbUtil.listDatabases();
    }

    public boolean exists(@NonNull String deckName) {
        return deckDbUtil.exists(deckName);
    }

    public void removeDatabase(@NonNull String deckName) {
        SyncDatabaseUtil.getInstance(appContext).closeDeckDatabase(deckName);
        deckDbUtil.removeDatabase(deckName);
    }

}