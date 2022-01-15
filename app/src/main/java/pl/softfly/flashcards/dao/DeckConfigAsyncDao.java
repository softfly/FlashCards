package pl.softfly.flashcards.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.DeckConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface DeckConfigAsyncDao {

    @NonNull
    @Insert
    Completable insert(DeckConfig deckConfig);

    @Query("SELECT * FROM DeckConfig WHERE `key`=:key")
    Maybe<DeckConfig> getByKey(String key);

    @Query("SELECT value FROM DeckConfig WHERE `key`=:key")
    Maybe<Long> getLongByKey(String key);
}
