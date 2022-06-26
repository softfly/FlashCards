package pl.softfly.flashcards.filesync.dao;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pl.softfly.flashcards.entity.deck.Card;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public abstract class CardDao extends pl.softfly.flashcards.dao.deck.CardDao {

    @Query("SELECT count(c.id) FROM Core_Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE t.id IS NULL")
    public abstract int countByCardImportedNull();

    @Query("SELECT c.* FROM Core_Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE " +
            "c.Term LIKE :term " +
            "AND c.definition LIKE :definition " +
            "AND t.cardId IS NULL " +
            "AND c.id NOT IN(:cardIds)" +
            "AND c.id NOT IN(SELECT cardId FROM FileSync_CardImportedRemoved WHERE fileSyncedId=:fileSyncedId)")
    public abstract Card findByTermLikeAndDefinitionLikeAndCardNull(String term, String definition, List<Integer> cardIds, int fileSyncedId);

    @NonNull
    @Query("SELECT c.* " +
            "FROM Core_Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE " +
            "t.id IS NULL " +
            "AND c.id > :idGreaterThan " +
            "AND c.id NOT IN(SELECT cardId FROM FileSync_CardImportedRemoved WHERE fileSyncedId=:fileSyncedId)" +
            "ORDER BY c.id " +
            "LIMIT 100")
    public abstract List<Card> findByCardImportedNullOrderById(int idGreaterThan, int fileSyncedId);

    @NonNull
    @Query("SELECT c.* " +
            "FROM Core_Card c " +
            "LEFT JOIN FileSync_CardImported i ON i.cardId=c.id " +
            "WHERE " +
            "c.deletedAt IS NULL " +
            "AND c.ordinal > :ordinalGreaterThan " +
            "AND c.id NOT IN(SELECT cardId FROM FileSync_CardImportedRemoved WHERE fileSyncedId=:fileSyncedId)" +
            "ORDER BY c.ordinal ASC " +
            "LIMIT 100")
    public abstract List<Card> findByOrdinalGreaterThanOrderByOrdinal(int ordinalGreaterThan, int fileSyncedId);

    @NonNull
    @Query("SELECT id FROM Core_Card ORDER BY ordinal ASC")
    public abstract List<Integer> getCardIdsOrderByOrdinalAsc();

    @NonNull
    @Query("SELECT c.id " +
            "FROM FileSync_CardImported i " +
            "JOIN Core_Card c ON c.id=i.cardId " +
            "WHERE c.deletedAt IS NOT null"
    )
    public abstract List<Integer> findByDeletedAtNotNull();

    @Query("UPDATE Core_Card SET ordinal=:ordinal WHERE id=:id")
    public abstract void updateOrdinal(int id, int ordinal);

}
