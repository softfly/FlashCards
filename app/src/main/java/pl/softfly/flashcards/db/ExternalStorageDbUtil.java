package pl.softfly.flashcards.db;

import android.content.Context;
import android.os.Environment;


import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pl.softfly.flashcards.BuildConfig;

/**
 * @author Grzegorz Ziemski
 */
public abstract class ExternalStorageDbUtil<DB extends RoomDatabase> extends AppStorageDbUtil<DB> {

    public ExternalStorageDbUtil(Context context) {
        super(context);
    }

    @Override
    public DB getDatabase(@NonNull String databaseName) {
        return Room.databaseBuilder(
                context,
                getTClass(),
                getDbFolder() + "/" + validateName(databaseName)
        ).build();
    }

    @Override
    protected String getDbFolder() {
        if (BuildConfig.DEBUG) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/FlashCards (DEV)/";
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/FlashCards/";
        }
    }

}
