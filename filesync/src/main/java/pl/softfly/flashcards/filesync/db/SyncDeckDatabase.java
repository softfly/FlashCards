package pl.softfly.flashcards.filesync.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import pl.softfly.flashcards.db.Converters;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.dao.CardDao;
import pl.softfly.flashcards.filesync.dao.CardEdgeDao;
import pl.softfly.flashcards.filesync.dao.CardImportedDao;
import pl.softfly.flashcards.filesync.dao.GraphEdgeDao;
import pl.softfly.flashcards.filesync.dao.GraphEdgeOnlyNewCardsDao;
import pl.softfly.flashcards.filesync.entity.CardEdge;
import pl.softfly.flashcards.filesync.entity.CardImported;
import pl.softfly.flashcards.filesync.view.CountToGraphOnlyNewCards;
import pl.softfly.flashcards.filesync.view.GraphEdge;
import pl.softfly.flashcards.filesync.view.GraphEdgeOnlyNewCards;

/**
 * @author Grzegorz Ziemski
 */
@Database(
        entities = {
                Card.class,
                CardImported.class,
                CardEdge.class
        },
        views = {
                GraphEdge.class,
                GraphEdgeOnlyNewCards.class,
                CountToGraphOnlyNewCards.class
        },
        version = 1)
@TypeConverters({Converters.class})
public abstract class SyncDeckDatabase extends RoomDatabase {

    public abstract CardDao cardDao();

    public abstract CardImportedDao cardImportedDao();

    public abstract CardEdgeDao cardEdgeDao();

    public abstract GraphEdgeDao graphEdgeDao();

    public abstract GraphEdgeOnlyNewCardsDao graphEdgeOnlyNewCardsDao();
}
