package pl.softfly.flashcards.dao;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import pl.softfly.flashcards.entity.DeckConfig;

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
