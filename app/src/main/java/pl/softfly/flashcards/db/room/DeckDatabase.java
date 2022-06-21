package pl.softfly.flashcards.db.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pl.softfly.flashcards.dao.deck.CardDao;
import pl.softfly.flashcards.dao.deck.CardDaoAsync;
import pl.softfly.flashcards.dao.deck.CardDaoLiveData;
import pl.softfly.flashcards.dao.deck.CardLearningProgressAsyncDao;
import pl.softfly.flashcards.dao.deck.DeckConfigAsyncDao;
import pl.softfly.flashcards.dao.deck.DeckConfigDao;
import pl.softfly.flashcards.dao.deck.DeckConfigLiveData;
import pl.softfly.flashcards.dao.filesync.FileSyncedAsyncDao;
import pl.softfly.flashcards.db.Converters;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.entity.deck.CardLearningProgress;
import pl.softfly.flashcards.entity.deck.DeckConfig;
import pl.softfly.flashcards.entity.filesync.CardEdge;
import pl.softfly.flashcards.entity.filesync.CardImported;
import pl.softfly.flashcards.entity.filesync.CardImportedRemoved;
import pl.softfly.flashcards.entity.filesync.FileSynced;
import pl.softfly.flashcards.view.filesync.CountToGraphOnlyNewCards;
import pl.softfly.flashcards.view.filesync.GraphEdge;
import pl.softfly.flashcards.view.filesync.GraphEdgeOnlyNewCards;

/**
 * @author Grzegorz Ziemski
 */
@Database(
        entities = {
                Card.class,
                CardLearningProgress.class,
                DeckConfig.class,
                CardImported.class,
                CardEdge.class,
                FileSynced.class,
                CardImportedRemoved.class
        },
        views = {
                GraphEdge.class,
                GraphEdgeOnlyNewCards.class,
                CountToGraphOnlyNewCards.class
        }, version = 1)
@TypeConverters({Converters.class})
public abstract class DeckDatabase extends RoomDatabase {

    public abstract CardDao cardDao();

    public abstract CardDaoAsync cardDaoAsync();

    public abstract CardDaoLiveData cardDaoLiveData();

    public abstract CardLearningProgressAsyncDao cardLearningProgressAsyncDao();

    public abstract DeckConfigDao deckConfigDao();

    public abstract DeckConfigAsyncDao deckConfigAsyncDao();

    public abstract DeckConfigLiveData deckConfigLiveData();

    public abstract FileSyncedAsyncDao fileSyncedDao();

}
