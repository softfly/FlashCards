package pl.softfly.flashcards.filesync.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pl.softfly.flashcards.filesync.view.GraphEdge;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface GraphEdgeDao {

    @Nullable
    @Query("SELECT * FROM FileSync_View_GraphEdge ORDER BY weight DESC LIMIT 1")
    GraphEdge getFirstOrderByWeightDesc();

    @NonNull
    @Query("SELECT * FROM FileSync_View_GraphEdge WHERE fromGraph=:fromGraph ORDER BY weight DESC")
    List<GraphEdge> findByFrom(int fromGraph);

}
