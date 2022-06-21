package pl.softfly.flashcards.dao.filesync;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Query;

import java.time.LocalDateTime;

import io.reactivex.rxjava3.core.Maybe;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface FileSyncedAsyncDao {

    @NonNull
    @Query("SELECT lastSyncAt FROM FileSync_FileSynced ORDER BY lastSyncAt DESC")
    Maybe<LocalDateTime> findLastSyncAt();

}
