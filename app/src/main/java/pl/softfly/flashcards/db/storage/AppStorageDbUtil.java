package pl.softfly.flashcards.db.storage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import pl.softfly.flashcards.db.AppDatabaseUtil;

/**
 * @author Grzegorz Ziemski
 */
public abstract class AppStorageDbUtil<DB extends RoomDatabase> implements StorageDb {

    protected Context context;

    public AppStorageDbUtil(Context context) {
        this.context = context;
    }

    public DB getDatabase(@NonNull String databaseName) {
        return Room.databaseBuilder(
                context,
                getTClass(),
                validateName(databaseName)
        ).build();
    }

    @NonNull
    public List<String> listDatabases() {
        List<String> deckNames = new LinkedList<>();
        File currentPath = new File(getDbFolder());
        File[] listFiles = currentPath.listFiles();
        if (listFiles != null) {
            for (File file : currentPath.listFiles()) {
                if (file.getName().endsWith(".db")) {
                    deckNames.add(file.getName().substring(0, file.getName().length() - 3));
                }
            }
        }
        return deckNames;
    }

    public boolean exists(@NonNull String databaseName) {
        return (new File(getDbFolder() + "/" + validateName(databaseName)))
                .exists();
    }

    public void removeDatabase(@NonNull String deckName) {
        if (exists(deckName)) {
            String mainPath = getDbFolder() + "/" + deckName;
            (new File(mainPath + ".db")).delete();
            (new File(mainPath + ".db-shm")).delete();
            (new File(mainPath + ".db-wal")).delete();
        }
    }
    public String findFreeDeckName(String deckName) {
        String freeDeckName = deckName;
        for (int i = 1; i <= 100; i++) {
            if (!exists(freeDeckName)) {
                return freeDeckName;
            }
            freeDeckName = deckName + " " + i;
        }
        throw new RuntimeException("No free deck name found.");
    }

    @NonNull
    protected String validateName(@NonNull String databaseName) {
        if (!databaseName.endsWith(".db")) {
            return databaseName + ".db";
        } else if (databaseName.toLowerCase().endsWith(".db")) {
            return databaseName.substring(0, databaseName.length() - 3) + ".db";
        }
        return databaseName;
    }

    public String getDbPath(@NonNull String deckName) {
        return getDbFolder() + "/" + deckName + ".db";
    }

    protected String getDbFolder() {
        return context.getFilesDir().getParentFile().getPath() + "/databases";
    }

    protected abstract Class<DB> getTClass();
}