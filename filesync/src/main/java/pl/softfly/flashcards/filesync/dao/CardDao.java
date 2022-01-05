package pl.softfly.flashcards.filesync.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pl.softfly.flashcards.entity.Card;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface CardDao extends pl.softfly.flashcards.dao.CardDao {

    @Query("SELECT count(c.id) FROM Core_Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE t.id IS NULL")
    int countByCardImportedNull();

    @Query("SELECT c.* FROM Core_Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE " +
            "c.Question LIKE :question " +
            "AND c.answer LIKE :answer " +
            "AND t.cardId IS NULL " +
            "AND c.id NOT IN(:cardIds)" +
            "AND c.id NOT IN(SELECT cardId FROM FileSync_CardImportedRemoved WHERE fileSyncedId=:fileSyncedId)")
    Card findByQuestionLikeAndAnswerLikeAndCardNull(String question, String answer, List<Integer> cardIds, int fileSyncedId);

    @Query("SELECT c.* " +
            "FROM Core_Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE " +
            "t.id IS NULL " +
            "AND c.id > :idGreaterThan " +
            "AND c.id NOT IN(SELECT cardId FROM FileSync_CardImportedRemoved WHERE fileSyncedId=:fileSyncedId)" +
            "ORDER BY c.id " +
            "LIMIT 100")
    List<Card> findByCardImportedNullOrderById(int idGreaterThan, int fileSyncedId);

    @Query("SELECT c.* " +
            "FROM Core_Card c " +
            "LEFT JOIN FileSync_CardImported i ON i.cardId=c.id " +
            "WHERE " +
            "c.ordinal > :ordinalGreaterThan " +
            "AND c.id NOT IN(SELECT cardId FROM FileSync_CardImportedRemoved WHERE fileSyncedId=:fileSyncedId)" +
            "ORDER BY c.ordinal ASC " +
            "LIMIT 100")
    List<Card> findByOrdinalGreaterThanOrderByOrdinal(int ordinalGreaterThan, int fileSyncedId);

    @Query("SELECT id FROM Core_Card ORDER BY ordinal ASC")
    List<Integer> getCardIdsOrderByOrdinalAsc();

    @Query("SELECT c.id " +
            "FROM FileSync_CardImported i " +
            "JOIN Core_Card c ON c.id=i.cardId " +
            "WHERE c.deletedAt IS NOT null"
    )
    List<Integer> findByDeletedAtNotNull();

    @Query("UPDATE Core_Card SET ordinal=:ordinal WHERE id=:id")
    void updateOrdinal(int id, int ordinal);

}
