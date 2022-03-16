package pl.softfly.flashcards.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.DeckConfig;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public abstract class DeckConfigAsyncDao {

    @NonNull
    @Insert
    public abstract Completable insert(DeckConfig deckConfig);

    @NonNull
    @Update
    public abstract Completable update(DeckConfig deckConfig);

    @NonNull
    @Query("SELECT * FROM DeckConfig WHERE `key`=:key")
    public abstract Maybe<DeckConfig> getByKey(String key);

    @NonNull
    @Query("SELECT value FROM DeckConfig WHERE `key`=:key")
    public abstract Maybe<Long> getLongByKey(String key);

    @NonNull
    @Query("SELECT value FROM DeckConfig WHERE `key`=:key")
    public abstract Maybe<Float> getFloatByKey(String key);

    public void updateDeckConfig(String key, String newValue, @NonNull Consumer<? super Throwable> onError) {
        getByKey(key)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(deckConfig -> {
                    deckConfig.setValue(newValue);
                    update(deckConfig)
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {}, onError);
                })
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        DeckConfig deckConfig = new DeckConfig();
                        deckConfig.setKey(key);
                        deckConfig.setValue(newValue);
                        insert(deckConfig)
                                .subscribeOn(Schedulers.io())
                                .subscribe(() -> {}, onError);
                    }
                })
                .subscribe(deckConfig -> {
                }, onError);
    }
}
