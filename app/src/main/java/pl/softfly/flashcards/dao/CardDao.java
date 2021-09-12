package pl.softfly.flashcards.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import pl.softfly.flashcards.entity.Card;

@Dao
public interface CardDao {

    @Query("SELECT count(*) FROM card")
    LiveData<Integer> count();

    @Query("SELECT * FROM card")
    LiveData<List<Card>> getCards();

    @Query("SELECT * FROM card")
    LiveData<Card> getNextCard();

    @Insert
    Completable insertAll(Card... cards);

    @Query("DELETE FROM Card")
    Completable deleteAll();
}
