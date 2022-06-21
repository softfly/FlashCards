package pl.softfly.flashcards.dao.app;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.entity.app.Deck;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 */
@Dao
public abstract class DeckDaoAsync {

    @Query("SELECT * FROM Deck WHERE path=:path")
    public abstract Maybe<Deck> findByKey(String path);

    @Query("SELECT * FROM Deck ORDER BY lastUpdatedAt DESC LIMIT :limit")
    public abstract Maybe<List<Deck>> findByLastUpdatedAt(int limit);

    @Query("DELETE FROM Deck WHERE `path`=:path")
    public abstract Completable deleteByPath(String path);

    @Query("DELETE FROM Deck WHERE `path` LIKE '%' || :path || '%'")
    public abstract Completable deleteByStartWithPath(String path);

    public Maybe<Deck> refreshLastUpdatedAt(String path) {
        if (!path.endsWith(".db")) {
            throw new RuntimeException("The database does not have the .db extension.");
        }

        return findByKey(path)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(deck -> {
                    deck.setLastUpdatedAt(TimeUtil.getNowEpochSec());
                    update(deck);
                })
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        Deck deck = new Deck();
                        deck.setName(getDeckName(path));
                        deck.setPath(path);
                        deck.setLastUpdatedAt(TimeUtil.getNowEpochSec());
                        insert(deck);
                    }
                });
    }

    @Query("UPDATE Deck SET path=:newPath WHERE path=:oldPath")
    public abstract Completable updatePathByPath(String newPath, String oldPath);

    @NonNull
    @Insert
    protected abstract long insert(Deck deck);

    @NonNull
    @Update
    protected abstract void update(Deck deck);

    @NonNull
    protected String getDeckName(@NonNull String deckDbPath) {
        return deckDbPath.substring(deckDbPath.lastIndexOf("/") + 1, deckDbPath.length() - 3);
    }

}
