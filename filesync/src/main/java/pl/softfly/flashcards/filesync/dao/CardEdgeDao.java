package pl.softfly.flashcards.filesync.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pl.softfly.flashcards.filesync.entity.CardEdge;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface CardEdgeDao {

    @Query("SELECT * FROM FileSync_CardEdge " +
            "WHERE " +
            "fromCardImportedId=:fromCardId " +
            "AND toCardImportedId=:toCardId")
    CardEdge findByVertices(int fromCardId, int toCardId);

    @Query("SELECT * FROM FileSync_CardEdge WHERE deleted=0 AND status IN (:statuses) ORDER BY weight DESC")
    CardEdge findByStatusOrderByWeightDesc(String[] statuses);

    @Insert
    void insertAll(CardEdge... cardEdge);

    @Insert
    void insertAll(List<CardEdge> cardEdge);

    @Update
    void updateAll(List<CardEdge> cardEdge);

    @Query("UPDATE FileSync_CardEdge SET deleted=1 WHERE id=:id")
    void delete(int id);

    @Query("UPDATE FileSync_CardEdge " +
            "SET deleted=1 " +
            "WHERE " +
            "fromCardImportedId=:fromCardImportedId " +
            "OR toCardImportedId=:toCardImportedId")
    void deleteByFromOrTo(int fromCardImportedId, int toCardImportedId);

    @Query("UPDATE FileSync_CardEdge SET deleted=1 " +
            "WHERE " +
            "id IN (" +
                "SELECT " +
                "e.id " +
                "FROM FileSync_CardEdge e " +
                "LEFT JOIN FileSync_CardImported f ON e.fromCardImportedId = f.id " +
                "LEFT JOIN FileSync_CardImported t ON e.toCardImportedId = t.id " +
                "WHERE deleted=0 AND f.graph = :graph AND t.graph = :graph" +
            ")"
    )
    void deleteInsideTheSameGraph(int graph);

    @Query("UPDATE FileSync_CardEdge SET deleted=1 " +
            "WHERE " +
            "id IN (" +
                "SELECT " +
                "e.id " +
                "FROM FileSync_CardEdge e " +
                "LEFT JOIN FileSync_CardImported f ON e.fromCardImportedId = f.id " +
                "LEFT JOIN FileSync_CardImported t ON e.toCardImportedId = t.id " +
                "WHERE " +
                "deleted=0 " +
                "AND (" +
                    "(" +
                        "f.newPreviousCardImportedId IS NOT NULL " +
                        "AND f.newNextCardImportedId IS NOT NULL " +
                        "AND f.graph = :graph" +
                    ") " +
                    "OR (" +
                        "t.newPreviousCardImportedId IS NOT NULL " +
                        "AND t.newNextCardImportedId IS NOT NULL " +
                        "AND t.graph = :graph" +
                    ")" +
                ")" +
            ")"
    )
    void deleteToMiddleGraph(int graph);

    @Query("DELETE FROM FileSync_CardEdge")
    void forceDeleteAll();
}