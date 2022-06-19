package pl.softfly.flashcards.db.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pl.softfly.flashcards.dao.app.AppConfigAsync;
import pl.softfly.flashcards.dao.app.AppConfigLiveData;
import pl.softfly.flashcards.dao.app.DeckDao;
import pl.softfly.flashcards.dao.app.DeckDaoAsync;
import pl.softfly.flashcards.db.Converters;
import pl.softfly.flashcards.entity.app.AppConfig;
import pl.softfly.flashcards.entity.app.Deck;

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
