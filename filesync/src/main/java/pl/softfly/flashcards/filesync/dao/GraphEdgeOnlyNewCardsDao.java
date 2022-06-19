package pl.softfly.flashcards.filesync.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pl.softfly.flashcards.view.filesync.GraphEdgeOnlyNewCards;

/**
 * Naming convention:
 * https://docs.spring.io/spring-data/jpa/docs/2.5.5/reference/html/#jpa.query-methods.query-creation
 *
 * @author Grzegorz Ziemski
 */
@Dao
public interface GraphEdgeOnlyNewCardsDao {

    @Nullable
    @Query("SELECT fromGraph, toGraph, weight FROM FileSync_View_GraphEdgeOnlyNewCards WHERE countToGraph >= 2 LIMIT 1")
    GraphEdgeOnlyNewCards findForNewCards();

    @NonNull
    @Query("SELECT fromGraph, toGraph, weight FROM FileSync_View_GraphEdgeOnlyNewCards WHERE fromGraph=:fromGraph")
    List<GraphEdgeOnlyNewCards> findForNewCardsByFrom(int fromGraph);

}
