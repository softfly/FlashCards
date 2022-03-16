package pl.softfly.flashcards.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import pl.softfly.flashcards.entity.Card;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public abstract class CardDaoLiveData {

    @Query("SELECT * FROM Core_Card WHERE id=:id")
    public abstract LiveData<Card> getCard(Integer id);
}
