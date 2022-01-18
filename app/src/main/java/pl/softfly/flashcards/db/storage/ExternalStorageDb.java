package pl.softfly.flashcards.db.storage;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pl.softfly.flashcards.R;

/**
 * Database files are stored in shared storage.
 *
 * @author Grzegorz Ziemski
 * @deprecated API level 30 (Android 11) blocks access to shared storage.
 */
public abstract class ExternalStorageDb<DB extends RoomDatabase> extends StorageDb {

    protected Context appContext;

    public ExternalStorageDb(Context appContext) {
        this.appContext = appContext;
    }

    @NonNull
    @Override
    public DB getDatabase(@NonNull String deckName) {
        return Room.databaseBuilder(
                appContext,
                getTClass(),
                getDbPath(deckName)
        ).build();
    }

    @NonNull
    @Override
    protected String getDbFolder() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + appContext.getResources().getString(R.string.app_name) + "/";
    }

    @NonNull
    @Override
    protected abstract Class<DB> getTClass();

}
