package pl.softfly.flashcards;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import pl.softfly.flashcards.dao.CardDao;
import pl.softfly.flashcards.entity.Card;

@Database(entities = {Card.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CardDao cardDao();
}
