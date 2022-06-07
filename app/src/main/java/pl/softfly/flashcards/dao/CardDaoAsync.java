package pl.softfly.flashcards.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDateTime;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.Card;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public abstract class CardDaoAsync {

    @NonNull
    @Query("SELECT count(*) FROM Core_Card")
    public abstract Maybe<Integer> count();

    @NonNull
    @Query("SELECT count(*) FROM Core_Card WHERE deletedAt is NULL")
    public abstract Maybe<Integer> countByNotDeleted();

    @NonNull
    @Query("SELECT * FROM Core_Card WHERE id=:id")
    public abstract Maybe<Card> getCard(Integer id);

    @NonNull
    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL ORDER BY ordinal ASC")
    public abstract Maybe<List<Card>> getCardsByDeletedNotOrderByOrdinal();

    @NonNull
    @Query("SELECT c.* FROM Core_Card c " +
            "LEFT JOIN Core_Card_LearningProgress l ON l.cardId = c.id " +
            "WHERE " +
            "c.deletedAt IS NULL " +
            "AND (l.nextReplayAt < strftime('%s', CURRENT_TIMESTAMP) OR l.nextReplayAt IS NULL) " +
            "ORDER BY l.nextReplayAt ASC, c.ordinal ASC " +
            "LIMIT 10")
    public abstract Maybe<List<Card>> getNextCards();

    @NonNull
    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL AND modifiedAt=:modifiedAt")
    public abstract Maybe<List<Card>> findByModifiedAt(LocalDateTime modifiedAt);

    @NonNull
    @Insert
    public abstract Completable insertAll(Card... cards);

    @NonNull
    @Update
    public abstract Completable updateAll(Card... cards);

    @NonNull
    @Query("UPDATE Core_Card " +
            "SET deletedAt=strftime('%s', CURRENT_TIMESTAMP) " +
            "WHERE id=:cardId")
    public abstract Completable delete(int cardId);

    @NonNull
    @Query("DELETE FROM Core_Card")
    public abstract Completable deleteAll();
}
