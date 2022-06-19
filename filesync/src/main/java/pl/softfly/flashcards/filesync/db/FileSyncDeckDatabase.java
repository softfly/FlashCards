package pl.softfly.flashcards.filesync.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pl.softfly.flashcards.dao.deck.DeckConfigAsyncDao;
import pl.softfly.flashcards.dao.deck.DeckConfigDao;
import pl.softfly.flashcards.db.Converters;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.entity.deck.CardLearningProgress;
import pl.softfly.flashcards.entity.deck.DeckConfig;
import pl.softfly.flashcards.filesync.dao.CardDao;
import pl.softfly.flashcards.filesync.dao.CardEdgeDao;
import pl.softfly.flashcards.filesync.dao.CardImportedDao;
import pl.softfly.flashcards.filesync.dao.CardImportedRemovedDao;
import pl.softfly.flashcards.filesync.dao.FileSyncedDao;
import pl.softfly.flashcards.filesync.dao.GraphEdgeDao;
import pl.softfly.flashcards.filesync.dao.GraphEdgeOnlyNewCardsDao;
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
        },
        exportSchema = false,
        version = 1
)
@TypeConverters({Converters.class})
public abstract class FileSyncDeckDatabase extends RoomDatabase {

    public abstract CardDao cardDao();

    public abstract CardImportedDao cardImportedDao();

    public abstract FileSyncedDao fileSyncedDao();

    public abstract CardImportedRemovedDao cardImportedRemovedDao();

    public abstract CardEdgeDao cardEdgeDao();

    public abstract GraphEdgeDao graphEdgeDao();

    public abstract GraphEdgeOnlyNewCardsDao graphEdgeOnlyNewCardsDao();

    public abstract DeckConfigDao deckConfigDao();

    public abstract DeckConfigAsyncDao deckConfigAsyncDao();
}
