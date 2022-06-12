package pl.softfly.flashcards.db.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pl.softfly.flashcards.dao.AppConfigAsync;
import pl.softfly.flashcards.dao.AppConfigLiveData;
import pl.softfly.flashcards.dao.DeckDao;
import pl.softfly.flashcards.dao.DeckDaoAsync;
import pl.softfly.flashcards.db.Converters;
import pl.softfly.flashcards.entity.AppConfig;
import pl.softfly.flashcards.entity.Deck;

/**
 * @author Grzegorz Ziemski
 */
@Database(
        entities = {
                AppConfig.class,
                Deck.class
        }, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppConfigLiveData appConfigLiveData();
    public abstract AppConfigAsync appConfigAsync();
    public abstract DeckDaoAsync deckDaoAsync();
    public abstract DeckDao deckDao();
}
