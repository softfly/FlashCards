package pl.softfly.flashcards.dao;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Completable;
import pl.softfly.flashcards.entity.AppConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public abstract class AppConfigLiveData {

    @Query("SELECT * FROM Core_App_Config WHERE `key`=:key")
    public abstract LiveData<AppConfig> findByKey(String key);
}
