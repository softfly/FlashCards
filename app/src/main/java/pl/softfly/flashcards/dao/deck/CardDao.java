package pl.softfly.flashcards.dao.deck;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.entity.deck.Card;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 */
@Dao
public abstract class CardDao {

    @Query("SELECT count(*) FROM Core_Card c WHERE deletedAt IS NULL")
    public abstract int count();

    @Query("SELECT max(ordinal) FROM Core_Card c WHERE deletedAt IS NULL")
    public abstract int lastOrdinal();

    @NonNull
    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL")
    public abstract List<Card> getCards();

    @NonNull
    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL ORDER BY ordinal ASC")
    public abstract List<Card> getCardsOrderByOrdinalAsc();

    @Nullable
    @Query("SELECT * FROM Core_Card WHERE deletedAt IS NULL ORDER BY ordinal ASC LIMIT 1")
    public abstract Card getFirst();

    @Query("SELECT * FROM Core_Card WHERE id=:id")
    public abstract Card findById(int id);

    @NonNull
    @Query("SELECT * FROM Core_Card WHERE id IN (:ids) ORDER BY ordinal")
    public abstract List<Card> findByIds(int[] ids);

    @Insert
    public abstract long insert(Card card);

    @Insert
    public abstract void insertAll(Card... cards);

    @Insert
    public abstract void insertAll(List<Card> cards);

    @Update
    public abstract void updateAll(Card... cards);

    @Update
    public abstract void updateAll(List<Card> cards);

    protected void decreaseOrdinalByBetween(int greaterThan, int lessThanEqual) {
        decreaseOrdinalByBetween(1, greaterThan, lessThanEqual);
    }

    @Query("UPDATE Core_Card SET ordinal=ordinal-:decrease " +
            "WHERE deletedAt IS NULL AND ordinal>:greaterThan AND ordinal<=:lessThanEqual")
    protected abstract void decreaseOrdinalByBetween(int decrease, int greaterThan, int lessThanEqual);

    protected void decreaseOrdinalByGreaterThan(int greaterThan) {
        decreaseOrdinalByGreaterThan(1, greaterThan);
    }

    @Query("UPDATE Core_Card SET ordinal=ordinal-:decrease " +
            "WHERE deletedAt IS NULL AND ordinal>:greaterThan")
    protected abstract void decreaseOrdinalByGreaterThan(int decrease, int greaterThan);

    protected void increaseOrdinalByBetween(int greaterThanEqual, int lessThan) {
        increaseOrdinalByBetween(1, greaterThanEqual, lessThan);
    }

    @Query("UPDATE Core_Card SET ordinal=ordinal+:increase " +
            "WHERE deletedAt IS NULL AND ordinal>=:greaterThanEqual AND ordinal<:lessThan")
    protected abstract void increaseOrdinalByBetween(int increase, int greaterThanEqual, int lessThan);

    protected void increaseOrdinalByGreaterThanEqual(int greaterThanEqual) {
        increaseOrdinalByGreaterThanEqual(1, greaterThanEqual);
    }

    @Query("UPDATE Core_Card SET ordinal=ordinal+:increase " +
            "WHERE deletedAt IS NULL AND ordinal>=:greaterThanEqual")
    protected abstract void increaseOrdinalByGreaterThanEqual(int increase, int greaterThanEqual);

    @Transaction
    public void changeCardOrdinal(@NonNull Card card, int newOrdinal) {
        if (card.getOrdinal() == newOrdinal) {
            throw new RuntimeException("Move to the same place.");
        } else if (newOrdinal > card.getOrdinal()) {
            decreaseOrdinalByBetween(card.getOrdinal(), newOrdinal);
            card.setOrdinal(newOrdinal);
        } else {
            increaseOrdinalByBetween(newOrdinal, card.getOrdinal());
            card.setOrdinal(newOrdinal);
        }
        card.setModifiedAt(TimeUtil.getNowEpochSec());
        updateAll(card);
    }

    @Transaction
    public void insertAfterOrdinal(@NonNull Card card, int afterOrdinal) {
        increaseOrdinalByGreaterThanEqual(afterOrdinal);
        card.setOrdinal(afterOrdinal);
        card.setModifiedAt(TimeUtil.getNowEpochSec());
        insertAll(card);
    }

    @Transaction
    public void insertAtEnd(@NonNull Card card) {
        card.setOrdinal(lastOrdinal() + 1);
        card.setModifiedAt(TimeUtil.getNowEpochSec());
        insertAll(card);
    }

    @NonNull
    @Query("UPDATE Core_Card " +
            "SET deletedAt=strftime('%s', CURRENT_TIMESTAMP) " +
            "WHERE id=:cardId")
    protected abstract void setDeletedAt(int cardId);

    /**
     * Delete and refresh ordinal numbers for all not removed cards.
     */
    @Transaction
    protected void delete(@NonNull Card card) {
        setDeletedAt(card.getId());
        decreaseOrdinalByGreaterThan(card.getOrdinal());
    }

    /**
     * Delete and refresh ordinal numbers for all not removed cards.
     */
    @NonNull
    public Completable deleteAsync(@NonNull Card card) {
        return Completable.fromAction(() -> delete(card));
    }

    /**
     * Delete and refresh ordinal numbers for all not removed cards.
     */
    @Transaction
    public void delete(@NonNull Collection<Card> cards) {
        List<Card> sorted = cards.stream()
                .sorted(Comparator.comparing(Card::getOrdinal))
                .collect(Collectors.toList());
        Card card1 = sorted.remove(0);
        int deleted = 1;
        for (Card card2 : sorted) {
            setDeletedAt(card1.getId());
            decreaseOrdinalByBetween(deleted++, card1.getOrdinal(), card2.getOrdinal());
            card1 = card2;
        }
        setDeletedAt(card1.getId());
        decreaseOrdinalByGreaterThan(deleted, card1.getOrdinal());
    }

    /**
     * Restore and refresh ordinal numbers.
     */
    @Transaction
    protected void restore(@NonNull Card card) {
        increaseOrdinalByGreaterThanEqual(card.getOrdinal());
        card.setDeletedAt(null);
        updateAll(card);
    }

    /**
     * Restore and refresh ordinal numbers.
     */
    @NonNull
    public Completable restoreAsync(@NonNull Card card) {
        return Completable.fromAction(() -> restore(card));
    }

    /**
     * Restore and refresh ordinal numbers.
     */
    @Transaction
    protected void restore(@NonNull List<Card> cards) {
        List<Card> sorted = cards.stream()
                .sorted(Comparator.comparing(Card::getOrdinal))
                .collect(Collectors.toList());
        for (Card card : sorted) {
            card.setDeletedAt(null);
            increaseOrdinalByGreaterThanEqual(1, card.getOrdinal());
            updateAll(card);
        }
    }

    /**
     * Restore and refresh ordinal numbers.
     */
    @NonNull
    public Completable restoreAsync(@NonNull List<Card> cards) {
        return Completable.fromAction(() -> restore(cards));
    }

    @Query("DELETE FROM Core_Card WHERE deletedAt IS NOT NULL")
    public abstract void purgeDeleted();
}
