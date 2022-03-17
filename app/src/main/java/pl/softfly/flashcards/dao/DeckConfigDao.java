package pl.softfly.flashcards.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.entity.DeckConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface DeckConfigDao {

    @Query("SELECT * FROM Core_Deck_Config WHERE `key`=:key")
    DeckConfig findByKey(String key);

    @NonNull
    @Insert
    long insert(DeckConfig deckConfig);

    @NonNull
    @Update
    void update(DeckConfig deckConfig);
}
