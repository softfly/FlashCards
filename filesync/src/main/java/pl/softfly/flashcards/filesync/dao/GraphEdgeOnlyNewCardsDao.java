package pl.softfly.flashcards.filesync.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pl.softfly.flashcards.filesync.view.GraphEdgeOnlyNewCards;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface GraphEdgeOnlyNewCardsDao {

    @Query("SELECT * FROM FileSync_View_GraphEdgeOnlyNewCards WHERE countToGraph >= 2 LIMIT 1")
    GraphEdgeOnlyNewCards findForNewCards();

    @Query("SELECT * FROM FileSync_View_GraphEdgeOnlyNewCards WHERE fromGraph=:fromGraph")
    List<GraphEdgeOnlyNewCards> findForNewCardsByFrom(int fromGraph);

}