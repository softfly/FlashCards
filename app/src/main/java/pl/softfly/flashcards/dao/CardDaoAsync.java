package pl.softfly.flashcards.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

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
    @Query("SELECT * FROM Core_Card WHERE id=:id")
    public abstract Maybe<Card> getCard(Integer id);

    @NonNull
    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL ORDER BY ordinal ASC")
    public abstract Maybe<List<Card>> getCardsByDeletedNotOrderByOrdinal();

    @NonNull
    @Query("SELECT * FROM Core_Card WHERE " +
            "Core_Card.nextReplayAt < strftime('%s', CURRENT_TIMESTAMP) " +
            "OR Core_Card.nextReplayAt IS NULL " +
            "LIMIT 10")
    public abstract Maybe<List<Card>> getNextCards();

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
