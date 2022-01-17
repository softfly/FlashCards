package pl.softfly.flashcards.db.storage;

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
public interface StorageDb<DB extends RoomDatabase> {

    DB getDatabase(String databaseName);

    List<String> listDatabases();

    boolean exists(String databaseName);

    void removeDatabase(String deckName);

}
