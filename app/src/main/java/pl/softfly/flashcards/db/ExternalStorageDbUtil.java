package pl.softfly.flashcards.db;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * @author Grzegorz Ziemski
 */
public abstract class ExternalStorageDbUtil<DB extends RoomDatabase> extends AppStorageDbUtil<DB> {

    private static final String PATH_DB_EXTERNAL = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/flashcards/";

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
        return PATH_DB_EXTERNAL;
    }

}
