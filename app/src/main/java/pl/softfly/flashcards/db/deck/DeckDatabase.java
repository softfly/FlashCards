package pl.softfly.flashcards.db.deck;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pl.softfly.flashcards.dao.CardDao;
import pl.softfly.flashcards.dao.CardDaoAsync;
import pl.softfly.flashcards.dao.CardLearningProgressAsyncDao;
import pl.softfly.flashcards.dao.DeckConfigAsyncDao;
import pl.softfly.flashcards.dao.DeckConfigDao;
import pl.softfly.flashcards.dao.FileSyncedAsyncDao;
import pl.softfly.flashcards.db.Converters;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.entity.CardLearningProgress;
import pl.softfly.flashcards.entity.DeckConfig;
import pl.softfly.flashcards.filesync.entity.CardEdge;
import pl.softfly.flashcards.filesync.entity.CardImported;
import pl.softfly.flashcards.filesync.entity.CardImportedRemoved;
import pl.softfly.flashcards.filesync.entity.FileSynced;
import pl.softfly.flashcards.filesync.view.CountToGraphOnlyNewCards;
import pl.softfly.flashcards.filesync.view.GraphEdge;
import pl.softfly.flashcards.filesync.view.GraphEdgeOnlyNewCards;

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

    public abstract CardLearningProgressAsyncDao cardLearningProgressAsyncDao();

    public abstract DeckConfigDao deckConfigDao();

    public abstract DeckConfigAsyncDao deckConfigAsyncDao();

    public abstract FileSyncedAsyncDao fileSyncedDao();

}
