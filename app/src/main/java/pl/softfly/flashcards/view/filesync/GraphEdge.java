package pl.softfly.flashcards.view.filesync;

import androidx.room.DatabaseView;

import pl.softfly.flashcards.entity.filesync.CardImported;

/**
 * @author Grzegorz Ziemski
 */
@DatabaseView(
        viewName = "FileSync_View_GraphEdge",
        value = "SELECT " +
                "f.graph as fromGraph, " +
                "t.graph as toGraph, " +
                "sum(e.weight) as weight " +
                "FROM FileSync_CardEdge e " +
                "LEFT JOIN FileSync_CardImported f ON e.fromCardImportedId = f.id " +
                "LEFT JOIN FileSync_CardImported t ON e.toCardImportedId = t.id " +
                "WHERE fromGraph != toGraph " +
                "AND f.contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "'," +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "') " +
                "AND t.contentStatus NOT IN (" +
                "'" + CardImported.STATUS_DELETE_BY_FILE + "'," +
                "'" + CardImported.STATUS_DELETE_BY_DECK + "') " +
                "GROUP BY f.graph, t.graph " +
                "ORDER BY weight DESC"
)
public class GraphEdge {

    protected Integer fromGraph;

    protected Integer toGraph;

    /**
     * The sum of edge weights between graphs.
     */
    protected int weight;

    public Integer getFromGraph() {
        return fromGraph;
    }

    public void setFromGraph(Integer fromGraph) {
        this.fromGraph = fromGraph;
    }

    public Integer getToGraph() {
        return toGraph;
    }

    public void setToGraph(Integer toGraph) {
        this.toGraph = toGraph;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
