package pl.softfly.flashcards.filesync.view;

import androidx.room.DatabaseView;

import pl.softfly.flashcards.filesync.entity.CardEdge;
import pl.softfly.flashcards.filesync.entity.CardImported;

/**
 * @author Grzegorz Ziemski
 */
@DatabaseView(
        viewName = "FileSync_View_GraphEdgeOnlyNewCards",
        value = "SELECT " +
                "f.graph as fromGraph, " +
                "t.graph as toGraph, " +
                "c.countToGraph as countToGraph, " +
                "e.weight as weight " +
                "FROM FileSync_CardEdge e " +
                "LEFT JOIN FileSync_CardImported f ON e.fromCardImportedId = f.id " +
                "LEFT JOIN FileSync_CardImported t ON e.toCardImportedId = t.id " +
                "LEFT JOIN FileSync_View_CountToGraphOnlyNewCards c ON c.fromGraph = f.graph " +
                "WHERE fromGraph != toGraph " +
                "AND f.contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "'," +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "') " +
                "AND t.contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "'," +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "') " +
                "AND e.status IN (" +
                "'" + CardEdge.STATUS_DECK_FIRST_NEW + "'," +
                "'" + CardEdge.STATUS_DECK_SECOND_NEW + "'," +
                "'" + CardEdge.STATUS_IMPORTED_FIRST_NEW + "'," +
                "'" + CardEdge.STATUS_IMPORTED_SECOND_NEW + "')" +
                "ORDER BY c.countToGraph DESC, weight DESC"
)
public class GraphEdgeOnlyNewCards extends GraphEdge {
}