package pl.softfly.flashcards.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.Deck;
import pl.softfly.flashcards.entity.DeckConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 */
@Dao
public interface DeckDao {

    @NonNull
    @Insert
    long insert(Deck deck);

    @NonNull
    @Update
    void update(Deck deck);

}
