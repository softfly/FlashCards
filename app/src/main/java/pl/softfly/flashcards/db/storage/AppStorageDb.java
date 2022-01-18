package pl.softfly.flashcards.db.storage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Database files are stored in app-specific storage.
 *
 * @author Grzegorz Ziemski
 */
public abstract class AppStorageDb<DB extends RoomDatabase> extends StorageDb {

    protected Context appContext;

    public AppStorageDb(Context appContext) {
        this.appContext = appContext;
    }

    @NonNull
    @Override
    public DB getDatabase(@NonNull String deckName) {
        return Room.databaseBuilder(
                appContext,
                getTClass(),
                addDbFilenameExtensionIfRequired(deckName)
        ).build();
    }

    @NonNull
    @Override
    protected String getDbFolder() {
        return appContext.getFilesDir().getParentFile().getPath() + "/databases";
    }

    @NonNull
    @Override
    protected abstract Class<DB> getTClass();
}