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

    @Query("SELECT count(c.id) FROM Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE t.id IS NULL")
    int countByCardImportedNull();

    @Query("SELECT c.* FROM Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE " +
            "c.Question LIKE :question " +
            "AND c.answer LIKE :answer " +
            "AND t.cardId IS NULL " +
            "AND c.id NOT IN(:cardIds)")
    Card findByQuestionLikeAndAnswerLikeAndCardNull(String question, String answer, List<Integer> cardIds);

    @Query("SELECT c.* FROM Card c " +
            "LEFT JOIN FileSync_CardImported t ON c.id = t.cardId " +
            "WHERE t.id IS NULL AND c.id > :idGreaterThan " +
            "ORDER BY c.id " +
            "LIMIT 100")
    List<Card> findByCardImportedNullOrderById(int idGreaterThan);

    @Query("SELECT c.* " +
            "FROM Card c " +
            "LEFT JOIN FileSync_CardImported i ON i.cardId=c.id " +
            "WHERE " +
            "c.ordinal > :ordinalGreaterThan " +
            "ORDER BY c.ordinal ASC " +
            "LIMIT 100")
    List<Card> findByOrdinalGreaterThanOrderByOrdinal(int ordinalGreaterThan);

    @Query("SELECT id FROM Card ORDER BY ordinal ASC")
    List<Integer> getCardIdsOrderByOrdinalAsc();

    @Query("UPDATE Card SET ordinal=:ordinal WHERE id=:id")
    void updateOrdinal(int id, int ordinal);

}
