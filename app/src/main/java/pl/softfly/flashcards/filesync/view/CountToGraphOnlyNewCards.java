package pl.softfly.flashcards.filesync.view;

import androidx.room.DatabaseView;

import pl.softfly.flashcards.filesync.entity.CardEdge;
import pl.softfly.flashcards.filesync.entity.CardImported;

/**
 * @author Grzegorz Ziemski
 */
@DatabaseView(
        viewName = "FileSync_View_CountToGraphOnlyNewCards",
        value = "SELECT " +
                "f.graph as fromGraph, " +
                "count(DISTINCT t.graph) as countToGraph " +
                "FROM FileSync_CardEdge e " +
                "LEFT JOIN FileSync_CardImported f ON e.fromCardImportedId = f.id " +
                "LEFT JOIN FileSync_CardImported t ON e.toCardImportedId = t.id " +
                "WHERE " +
                "f.graph != t.graph " +
                "AND e.deleted=0 " +
                "AND e.status IN (" +
                "'" + CardEdge.STATUS_DECK_FIRST_NEW + "'," +
                "'" + CardEdge.STATUS_DECK_SECOND_NEW + "'," +
                "'" + CardEdge.STATUS_IMPORTED_FIRST_NEW + "'," +
                "'" + CardEdge.STATUS_IMPORTED_SECOND_NEW + "')" +
                "AND f.contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "'," +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "') " +
                "AND t.contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "'," +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "') " +
                "GROUP BY fromGraph")
public class CountToGraphOnlyNewCards {

    private int fromGraph;

    private int countToGraph;

    public int getFromGraph() {
        return fromGraph;
    }

    public void setFromGraph(int fromGraph) {
        this.fromGraph = fromGraph;
    }

    public int getCountToGraph() {
        return countToGraph;
    }

    public void setCountToGraph(int countToGraph) {
        this.countToGraph = countToGraph;
    }
}
/*
SELECT
f.subGraph as fromSubGraph,
t.subGraph as toSubGraph,
c.countToSubGraph as countToSubGraph
FROM CardEdge e
LEFT JOIN CardImported f ON e.fromCardImportedId = f.id
LEFT JOIN CardImported t ON e.toCardImportedId = t.id
LEFT JOIN CountToSubGraphNewCardsView c ON c.fromSubGraph = f.subGraph
WHERE fromSubGraph != toSubGraph
AND f.status NOT IN ('DELETE_BY_FILE','DELETE_BY_DECK')
AND t.status NOT IN ('DELETE_BY_FILE','DELETE_BY_DECK')
AND e.status IN (
'DECK_FIRST_NEW',
'DECK_SECOND_NEW',
'IMPORTED_FIRST_NEW',
'IMPORTED_SECOND_NEW')
ORDER BY countToSubGraph DESC, weight DESC
 */