package pl.softfly.flashcards.dao.app;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import pl.softfly.flashcards.entity.app.AppConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public abstract class AppConfigAsync {

    @Query("SELECT * FROM Core_App_Config WHERE `key`=:key")
    public abstract Maybe<AppConfig> findByKey(String key);

    @Query("DELETE FROM Core_App_Config WHERE `key`=:key")
    public abstract Completable deleteByKey(String key);

    @NonNull
    @Insert
    public abstract Completable insertAll(AppConfig... appConfig);

    @NonNull
    @Update
    public abstract Completable updateAll(AppConfig... appConfig);

}
