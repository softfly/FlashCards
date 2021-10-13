package pl.softfly.flashcards.filesync.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pl.softfly.flashcards.filesync.entity.CardEdge;
import pl.softfly.flashcards.filesync.entity.CardImported;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface CardImportedDao {

    @Query("SELECT count(*) FROM FileSync_CardImported WHERE id=:id AND graph=:graph")
    int countByIdAndGraph(int id, int graph);

    @Query("SELECT count(*) FROM FileSync_CardImported " +
            "WHERE " +
            "newOrdinal IS NULL " +
            "AND contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "'," +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "'" +
            ")")
    int countByNewOrdinalNull();

    @Query("SELECT count(*) FROM FileSync_CardImported WHERE orderChanged=1")
    int countByOrderChangedTrue();

    @Query("SELECT * FROM FileSync_CardImported")
    List<CardImported> getCards();

    @Query("SELECT * FROM FileSync_CardImported ORDER BY id ASC LIMIT 1")
    CardImported getFirst();

    @Query("SELECT * FROM FileSync_CardImported WHERE id > :idGreaterThan LIMIT 100")
    List<CardImported> findAll(int idGreaterThan);

    @Query("SELECT * FROM FileSync_CardImported WHERE id=:id")
    CardImported findById(int id);

    @Query("SELECT * FROM FileSync_CardImported WHERE cardId=:cardId")
    CardImported findByCardId(int cardId);

    @Query("SELECT id FROM FileSync_CardImported WHERE newPreviousCardImportedId IS NULL AND graph=:graph")
    Integer findFirstVertex(int graph);

    @Query("SELECT id FROM FileSync_CardImported WHERE newNextCardImportedId IS NULL AND graph=:graph")
    Integer findLastVertex(int graph);

    @Query("SELECT * FROM FileSync_CardImported " +
            "WHERE " +
            "cardId IS NULL " +
            "AND id > :idGreaterThan " +
            "ORDER BY id ASC " +
            "LIMIT :limit")
    List<CardImported> findByCardNull(int idGreaterThan, int limit);

    @Query("SELECT * FROM FileSync_CardImported " +
            "WHERE " +
            "ordinal > :ordinalGreaterThan " +
            "AND contentStatus NOT IN (:statuses) " +
            "ORDER BY ordinal ASC " +
            "LIMIT 100")
    List<CardImported> findByStatusNotOrderByOrdinalAsc(String[] statuses, int ordinalGreaterThan);

    @Query("SELECT i.* FROM FileSync_CardImported i " +
            "JOIN Card c ON i.cardId = c.id " +
            "WHERE " +
            "c.ordinal > :ordinalGreaterThan " +
            "AND i.contentStatus NOT IN (:statuses) " +
            "ORDER BY c.ordinal ASC " +
            "LIMIT 100")
    List<CardImported> findByStatusNotOrderByCardOrdinalAsc(String[] statuses, int ordinalGreaterThan);

    @Query("SELECT * FROM FileSync_CardImported " +
            "WHERE " +
            "newPreviousCardImportedId IS NULL " +
            "AND contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "'," +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "') " +
            "LIMIT 3")
    List<CardImported> findByStatusNotDeletedAndNewPreviousCardImportedIdIsNull();

    @Query("SELECT * FROM FileSync_CardImported " +
            "WHERE " +
            "newPreviousCardImportedId IS NULL " +
            "AND contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "'," +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "') " +
            "LIMIT 3")
    List<CardImported> findByStatusNotDeletedAndNewNextCardImportedIdIsNull();

    @Query("SELECT * FROM FileSync_CardImported " +
            "WHERE " +
            "graph IS NULL " +
            "AND contentStatus NOT IN (:statuses)")
    List<CardImported> findByStatusNotAndGraphNull(String[] statuses);

    @Query("SELECT i.id " +
            "FROM FileSync_CardImported i " +
            "JOIN FileSync_CardEdge e ON e.fromCardImportedId = i.id OR e.toCardImportedId = i.id " +
            "WHERE " +
            "i.graph IS NULL " +
            "AND e.status IN (" +
                "'" + CardEdge.STATUS_DECK_FIRST_NEW + "'," +
                "'" + CardEdge.STATUS_DECK_SECOND_NEW + "'," +
                "'" + CardEdge.STATUS_IMPORTED_FIRST_NEW + "'," +
                "'" + CardEdge.STATUS_IMPORTED_SECOND_NEW + "')" +
            "AND i.contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "'," +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "') " +
            "GROUP BY i.id " +
            "ORDER BY i.id")
    List<Integer> findByCardEdgeStatusNew();

    @Query("SELECT max(graph) FROM FileSync_CardImported")
    Integer maxGraph();

    @Query("SELECT graph FROM FileSync_CardImported " +
            "WHERE " +
            "contentStatus NOT IN (" +
            "'" + CardImported.STATUS_DELETE_BY_DECK + "'," +
            "'" + CardImported.STATUS_DELETE_BY_FILE + "') " +
            "GROUP BY graph")
    List<Integer> getGraphs();

    @Insert
    void insertAll(CardImported... cards);

    @Insert
    void insertAll(List<CardImported> cards);

    @Update
    void updateAll(CardImported... cards);

    @Update
    void updateAll(List<CardImported> cards);

    @Query("UPDATE FileSync_CardImported SET graph=:graph, debugFirstGraph=:graph WHERE id=:id")
    void updateGraphById(int id, int graph);

    @Query("UPDATE FileSync_CardImported SET graph=:newGraph WHERE graph=:oldGraph")
    void updateGraphByGraph(int oldGraph, int newGraph);

    @Query("UPDATE FileSync_CardImported SET newPreviousCardImportedId=:newPreviousCardImportedId WHERE id=:id")
    void updateNewPreviousCardImportedIdById(int id, int newPreviousCardImportedId);

    @Query("UPDATE FileSync_CardImported SET newNextCardImportedId=:newNextCardImportedId WHERE id=:id")
    void updateNewNextCardImportedIdById(int id, int newNextCardImportedId);

    @Query("UPDATE FileSync_CardImported " +
            "SET cardId=null " +
            "WHERE " +
            "cardId IN (SELECT cardId FROM FileSync_CardImported s GROUP BY cardId HAVING count(*) > 1)")
    void clearMultipleAssignedCards();

    @Query("DELETE FROM FileSync_CardImported")
    void deleteAll();
}
