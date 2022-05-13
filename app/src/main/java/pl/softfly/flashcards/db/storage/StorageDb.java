package pl.softfly.flashcards.db.storage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
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

    protected Context appContext;

    public StorageDb(Context appContext) {
        this.appContext = appContext;
    }

    /**
     * @param name May be with or without .db at the end.
     */
    @NonNull
    public DB getDatabase(@NonNull File folder, String name) {
        return getDatabase(folder.getPath(), name);
    }

    @NonNull
    public DB getDatabase(@NonNull String folder, String name) {
        return getDatabase(folder + "/" + name);
    }

    /**
     * @param path May be with or without .db at the end.
     * @todo Create a separate create method to prevent accidental creation nonexistent DB.
     */
    @NonNull
    public DB getDatabase(@NonNull String path) {
        return Room.databaseBuilder(
                appContext,
                getTClass(),
                addDbFilenameExtensionIfRequired(path)
        ).build();
    }

    @NonNull
    public List<File> listFolders(@NonNull File path) {
        List<File> folders = new LinkedList<>();
        File[] listFiles = path.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    folders.add(file);
                }
            }
        }
        return folders;
    }

    /**
     * @return Deck names without .db at the end.
     */
    @NonNull
    public List<String> listDatabases(@NonNull File path) {
        List<String> deckNames = new LinkedList<>();
        File[] listFiles = path.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.getName().endsWith(".db")) {
                    deckNames.add(file.getName().substring(0, file.getName().length() - 3));
                }
            }
        }
        return deckNames;
    }

    public boolean exists(@NonNull File folder, @NonNull String name) {
        return exists(folder.getPath(), name);
    }

    public boolean exists(@NonNull String folder, @NonNull String name) {
        return exists(folder + "/" + name);
    }

    public boolean exists(@NonNull String path) {
        return (new File(addDbFilenameExtensionIfRequired(path))).exists();
    }

    public boolean removeDatabase(@NonNull File folder, @NonNull String name) {
        return removeDatabase(folder.getPath(), name);
    }

    public boolean removeDatabase(@NonNull String folder, @NonNull String name) {
        return removeDatabase(folder + "/" + name);
    }

    public boolean removeDatabase(@NonNull String path) {
        path = addDbFilenameExtensionIfRequired(path);
        if (exists(path)) {
            (new File(path)).delete();
            (new File(path + "-shm")).delete();
            (new File(path + "-wal")).delete();
            return true;
        }
        return false;
    }

    public String findFreeName(@NonNull File folder, @NonNull String name) {
        return findFreeName(folder.getPath(), name);
    }

    public String findFreeName(@NonNull String folder, @NonNull String name) {
        return findFreeName(folder + "/" + name);
    }

    public String findFreeName(String path) {
        String freeName = path;
        for (int i = 1; i <= 100; i++) {
            if (!exists(freeName)) {
                return freeName;
            }
            freeName = path + " " + i;
        }
        throw new RuntimeException("No free deck name found.");
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
    public abstract String getDbFolder();

    @NonNull
    protected abstract Class<DB> getTClass();

}
