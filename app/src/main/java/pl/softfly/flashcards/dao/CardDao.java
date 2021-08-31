package pl.softfly.flashcards.dao;

import androidx.room.Dao;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Completable;

@Dao
public interface CardDao {

    @Query("DELETE FROM Card")
    Completable deleteAll();

}
