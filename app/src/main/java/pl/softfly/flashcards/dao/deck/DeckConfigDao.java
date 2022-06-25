package pl.softfly.flashcards.dao.deck;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import pl.softfly.flashcards.entity.deck.DeckConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface DeckConfigDao {

    @Query("SELECT * FROM Core_DeckConfig WHERE `key`=:key")
    DeckConfig findByKey(String key);

    @NonNull
    @Insert
    long insert(DeckConfig deckConfig);

    @NonNull
    @Update
    void update(DeckConfig deckConfig);

    @NonNull
    @Delete
    void delete(DeckConfig deckConfig);

    @NonNull
    @Query("DELETE FROM Core_DeckConfig " +
            "WHERE `key`=:key")
    void deleteByKey(String key);
}
