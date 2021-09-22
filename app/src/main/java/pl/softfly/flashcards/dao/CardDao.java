package pl.softfly.flashcards.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.Card;

@Dao
public interface CardDao {

    @Query("SELECT count(*) FROM Card")
    LiveData<Integer> count();

    @Query("SELECT * FROM Card WHERE Card.id= :id")
    LiveData<Card> getCard(Integer id);

    @Query("SELECT * FROM Card")
    LiveData<List<Card>> getCards();

    @Query("SELECT * FROM Card WHERE card.nextReplayAt < strftime('%s', CURRENT_TIMESTAMP) OR card.nextReplayAt IS NULL LIMIT 10")
    Maybe<List<Card>> getNextCards();

    @Insert
    Completable insertAll(Card... cards);

    @Insert
    Completable insertAll(List<Card> cards);

    @Update
    Completable updateAll(Card... cards);

    @Delete
    Completable delete(Card card);

    @Query("DELETE FROM Card")
    Completable deleteAll();
}
