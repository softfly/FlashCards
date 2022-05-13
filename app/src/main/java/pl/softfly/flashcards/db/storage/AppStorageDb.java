package pl.softfly.flashcards.db.storage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;

/**
 * Database files are stored in app-specific storage.
 *
 * @author Grzegorz Ziemski
 */
public abstract class AppStorageDb<DB extends RoomDatabase> extends StorageDb {

    public AppStorageDb(Context appContext) {
        super(appContext);
    }

    @NonNull
    @Override
    public String getDbFolder() {
        return appContext.getFilesDir().getParentFile().getPath() + "/databases/decks";
    }

    @NonNull
    @Override
    protected abstract Class<DB> getTClass();
}