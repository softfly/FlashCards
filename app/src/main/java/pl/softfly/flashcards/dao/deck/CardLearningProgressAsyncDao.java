package pl.softfly.flashcards.dao.deck;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.deck.CardLearningProgress;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface CardLearningProgressAsyncDao {

    @NonNull
    @Query("SELECT * FROM Core_CardLearningProgress WHERE cardId=:cardId")
    Maybe<CardLearningProgress> findByCardId(Integer cardId);

    @NonNull
    @Insert
    Completable insertAll(CardLearningProgress... cards);

    @NonNull
    @Update
    Completable updateAll(CardLearningProgress... cardLearningProgress);
}
