package pl.softfly.flashcards.dao.deck;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import pl.softfly.flashcards.entity.deck.DeckConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface DeckConfigLiveData {

    @Query("SELECT * FROM Core_Deck_Config WHERE `key`=:key")
    LiveData<DeckConfig> findByKey(String key);
}
