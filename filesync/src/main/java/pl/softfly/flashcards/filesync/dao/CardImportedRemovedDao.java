package pl.softfly.flashcards.filesync.dao;

import androidx.room.Dao;
import androidx.room.Query;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface CardImportedRemovedDao {

    @Query("SELECT count(*) FROM FileSync_CardImportedRemoved WHERE fileSyncedId=:fileSyncedId AND cardId=:cardId")
    int count(int fileSyncedId, int cardId);

    @Query("INSERT INTO FileSync_CardImportedRemoved (fileSyncedId, cardId) VALUES (:fileSyncedId, :cardId)")
    void insert(int fileSyncedId, int cardId);

}
