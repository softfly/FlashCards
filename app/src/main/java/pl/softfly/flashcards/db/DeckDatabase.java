package pl.softfly.flashcards.db;

import android.content.Context;
import android.os.Environment;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import pl.softfly.flashcards.dao.CardDao;
import pl.softfly.flashcards.entity.Card;

@Database(entities = {Card.class}, version = 1)
public abstract class DeckDatabase extends RoomDatabase {

    public static DeckDatabase getDatabase(Context context, String dbName) {
        if (!dbName.endsWith(".db")) {
            dbName += ".db";
        } else if (dbName.toLowerCase().endsWith(".db")) {
            dbName = dbName.substring(0, dbName.length() - 3) + ".db";
        }
        return Room.databaseBuilder(
                context,
                DeckDatabase.class,
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/flashcards/" + dbName)
                .build();
    }

    public abstract CardDao cardDao();
}
