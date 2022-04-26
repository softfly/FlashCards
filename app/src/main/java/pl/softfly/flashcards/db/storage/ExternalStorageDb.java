package pl.softfly.flashcards.db.storage;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;

import pl.softfly.flashcards.R;

/**
 * Database files are stored in shared storage.
 * Only used for testing / development purposes.
 *
 * @author Grzegorz Ziemski
 * @deprecated API level 30 (Android 11) blocks access to shared storage.
 */
public abstract class ExternalStorageDb<DB extends RoomDatabase> extends StorageDb {

    public ExternalStorageDb(Context appContext) {
        super(appContext);
    }

    @NonNull
    @Override
    public String getDbFolder() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + appContext.getResources().getString(R.string.app_name) + "/";
    }

    @NonNull
    @Override
    protected abstract Class<DB> getTClass();

}
