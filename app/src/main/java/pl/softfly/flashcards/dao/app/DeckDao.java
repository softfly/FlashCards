package pl.softfly.flashcards.dao.app;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;

import pl.softfly.flashcards.entity.app.Deck;

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
