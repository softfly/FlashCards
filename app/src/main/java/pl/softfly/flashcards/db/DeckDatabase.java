package pl.softfly.flashcards.db;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import pl.softfly.flashcards.dao.CardDao;
import pl.softfly.flashcards.entity.Card;

@Database(entities = {Card.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class DeckDatabase extends RoomDatabase {

    private static final String PATH_DB = Environment.getExternalStorageDirectory().getAbsolutePath() + "/flashcards/";

    public static DeckDatabase getDatabase(Context context, @NonNull String deckName) {
        if (!deckName.endsWith(".db")) {
            deckName += ".db";
        } else if (deckName.toLowerCase().endsWith(".db")) {
            deckName = deckName.substring(0, deckName.length() - 3) + ".db";
        }
        return Room.databaseBuilder(
                context,
                DeckDatabase.class,
                PATH_DB + deckName)
                .build();
    }

    public static List<String> listDatabases() {
        List<String> deckNames = new LinkedList<>();
        File currentPath = new File(PATH_DB);
        for (File file : currentPath.listFiles()) {
            if (file.getName().endsWith(".db")) {
                deckNames.add(file.getName().substring(0, file.getName().length() - 3));
            }
        }
        return deckNames;
    }

    public static boolean exists(Context context, @NonNull String deckName) {
        if (!deckName.endsWith(".db")) {
            deckName += ".db";
        } else if (deckName.toLowerCase().endsWith(".db")) {
            deckName = deckName.substring(0, deckName.length() - 3) + ".db";
        }
        return (new File(PATH_DB + deckName)).exists();
    }

    public static void removeDatabase(String deckName) {
        String mainPath = PATH_DB + deckName;
        (new File(mainPath + ".db")).delete();
        (new File(mainPath + ".db-shm")).delete();
        (new File(mainPath + ".db-wal")).delete();
    }

    public abstract CardDao cardDao();
}
