package pl.softfly.flashcards.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pl.softfly.flashcards.entity.Card;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 */
@Dao
public interface CardDao {

    @Query("SELECT count(*) FROM Card c")
    int count();

    @Query("SELECT * FROM Card")
    List<Card> getCards();

    @Query("SELECT * FROM Card ORDER BY ordinal ASC")
    List<Card> getCardsOrderByOrdinalAsc();

    @Query("SELECT * FROM Card ORDER BY ordinal ASC LIMIT 1")
    Card getFirst();

    @Query("SELECT * FROM Card WHERE id=:id")
    Card findById(int id);

    @Insert
    long insert(Card card);

    @Insert
    void insertAll(Card... cards);

    @Insert
    void insertAll(List<Card> cards);

    @Update
    void updateAll(Card... cards);

    @Update
    void updateAll(List<Card> cards);

    @Delete
    void delete(Card card);

    @Delete
    void deleteAll(List<Card> card);

}
