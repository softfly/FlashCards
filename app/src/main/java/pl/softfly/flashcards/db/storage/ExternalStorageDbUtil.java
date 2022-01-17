package pl.softfly.flashcards.db.storage;

import android.content.Context;
import android.os.Environment;


import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pl.softfly.flashcards.BuildConfig;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.storage.AppStorageDbUtil;

/**
 * @author Grzegorz Ziemski
 * @deprecated API level 30 (Android 11) blocks access to shared storage.
 */
public abstract class ExternalStorageDbUtil<DB extends RoomDatabase> extends AppStorageDbUtil<DB> {

    public ExternalStorageDbUtil(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public DB getDatabase(@NonNull String databaseName) {
        return Room.databaseBuilder(
                context,
                getTClass(),
                getDbFolder() + "/" + validateName(databaseName)
        ).build();
    }

    @NonNull
    @Override
    protected String getDbFolder() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + context.getResources().getString(R.string.app_name) + "/";
    }

}
