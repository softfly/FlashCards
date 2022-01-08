package pl.softfly.flashcards.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import pl.softfly.flashcards.entity.Card;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 */
@Dao
public abstract class CardDao {

    @Query("SELECT count(*) FROM Core_Card c WHERE deletedAt IS NULL")
    public abstract int count();

    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL")
    public abstract List<Card> getCards();

    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL ORDER BY ordinal ASC")
    public abstract List<Card> getCardsOrderByOrdinalAsc();

    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL ORDER BY ordinal ASC LIMIT 1")
    public abstract Card getFirst();

    @Query("SELECT * FROM Core_Card WHERE id=:id")
    public abstract Card findById(int id);

    @Insert
    public abstract long insert(Card card);

    @Insert
    public abstract void insertAll(Card... cards);

    @Insert
    public abstract void insertAll(List<Card> cards);

    @Update
    public abstract void updateAll(Card... cards);

    @Update
    public abstract void updateAll(List<Card> cards);

    /**
     * @todo what timezone is used by SQLlite?
     */
    @Query("UPDATE Core_Card " +
            "SET ordinal=ordinal+1, modifiedAt=strftime('%s', CURRENT_TIMESTAMP) " +
            "WHERE ordinal>:newOrdinal AND ordinal<:oldOrdinal")
    protected abstract void increaseOrdinalByBetween(int newOrdinal, int oldOrdinal);

    @Query("UPDATE Core_Card " +
            "SET ordinal=ordinal-1, modifiedAt=strftime('%s', CURRENT_TIMESTAMP) " +
            "WHERE ordinal>:oldOrdinal AND ordinal<=:newOrdinal")
    protected abstract void decreaseOrdinalByBetweenEqual(int oldOrdinal, int newOrdinal);

    @Transaction
    public void changeCardOrdinal(Card card, int afterOrdinal) {
        if (afterOrdinal > card.getOrdinal()) {
            decreaseOrdinalByBetweenEqual(card.getOrdinal(), afterOrdinal);
            card.setOrdinal(afterOrdinal);
        } else {
            increaseOrdinalByBetween(afterOrdinal, card.getOrdinal());
            card.setOrdinal(afterOrdinal+1);
        }
        updateAll(card);
    }

    @Delete
    public abstract void delete(Card card);

    @Delete
    public abstract void deleteAll(List<Card> card);

    @Query("DELETE FROM Core_Card WHERE deletedAt IS NOT NULL")
    public abstract void purgeDeleted();



}
