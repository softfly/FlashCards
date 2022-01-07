package pl.softfly.flashcards.filesync.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.entity.FileSynced;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface FileSyncedDao {

    @Query("SELECT * FROM FileSync_FileSynced WHERE uri=:uri")
    FileSynced findByUri(String uri);

    @Query("SELECT * FROM FileSync_FileSynced WHERE uri=:uri")
    Maybe<FileSynced> findByUriAsync(String uri);

    @Insert
    long insert(FileSynced fileSynced);

    @Update
    void updateAll(FileSynced... fileSynced);

    @Query("UPDATE FileSync_FileSynced SET autoSync=0 WHERE id!=:id")
    void disableAutoSyncByIdNot(int id);

}
