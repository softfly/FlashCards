package pl.softfly.flashcards.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.Card;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 */
@Dao
public interface CardDaoAsync {

    @Query("SELECT count(*) FROM Core_Card")
    Maybe<Integer> count();

    @Query("SELECT * FROM Core_Card WHERE id=:id")
    Maybe<Card> getCard(Integer id);

    @Query("SELECT * FROM Core_Card")
    Maybe<List<Card>> getCards();

    @Query("SELECT * FROM Core_Card WHERE Core_Card.nextReplayAt < strftime('%s', CURRENT_TIMESTAMP) OR Core_Card.nextReplayAt IS NULL LIMIT 10")
    Maybe<List<Card>> getNextCards();

    @Insert
    Completable insertAll(Card... cards);

    @Update
    Completable updateAll(Card... cards);

    @Delete
    Completable delete(Card card);

    @Query("DELETE FROM Core_Card")
    Completable deleteAll();
}
