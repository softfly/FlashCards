package pl.softfly.flashcards.db.storage;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Database file management like delete, open db, close db, check the list of available dbs.
 *
 * @author Grzegorz Ziemski
 */
public abstract class StorageDb<DB extends RoomDatabase> {

    @NonNull
    public abstract DB getDatabase(@NonNull String deckName);

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

    public boolean exists(@NonNull String deckName) {
        return (new File(getDbFolder() + "/" + addDbFilenameExtensionIfRequired(deckName)))
                .exists();
    }

    public boolean removeDatabase(@NonNull String deckName) {
        if (exists(deckName)) {
            String mainPath = getDbFolder() + "/" + addDbFilenameExtensionIfRequired(deckName);
            (new File(mainPath)).delete();
            (new File(mainPath + "-shm")).delete();
            (new File(mainPath + "-wal")).delete();
            return true;
        }
        return false;
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
    public String getDbPath(@NonNull String deckName) {
        return getDbFolder() + "/" + addDbFilenameExtensionIfRequired(deckName);
    }

    @NonNull
    protected String addDbFilenameExtensionIfRequired(@NonNull String deckName) {
        if (!deckName.endsWith(".db")) {
            return deckName + ".db";
        } else if (deckName.toLowerCase().endsWith(".db")) {
            return deckName.substring(0, deckName.length() - 3) + ".db";
        }
        return deckName;
    }

    @NonNull
    protected abstract String getDbFolder();

    @NonNull
    protected abstract Class<DB> getTClass();

}
