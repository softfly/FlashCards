package pl.softfly.flashcards.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import pl.softfly.flashcards.entity.Card;

@Dao
public interface CardDao {

    @Query("SELECT count(*) FROM card")
    LiveData<Integer> count();

    @Query("SELECT * FROM card WHERE card.id= :id")
    LiveData<Card> getCard(Integer id);

    @Query("SELECT * FROM card")
    LiveData<List<Card>> getCards();

    @Query("SELECT * FROM card WHERE card.id > :id")
    LiveData<Card> getNextCard(Integer id);

    @Insert
    Completable insertAll(Card... cards);

    @Update
    Completable updateAll(Card... cards);

    @Delete
    Completable delete(Card card);

    @Query("DELETE FROM Card")
    Completable deleteAll();
}
