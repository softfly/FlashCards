package pl.softfly.flashcards.dao.app;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import pl.softfly.flashcards.entity.app.AppConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public abstract class AppConfigLiveData {

    @Query("SELECT * FROM AppConfig WHERE `key`=:key")
    public abstract LiveData<AppConfig> findByKey(String key);
}
