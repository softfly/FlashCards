package pl.softfly.flashcards.entity.filesync;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * The entity is used to determine the new order of the cards after synchronization with the imported file.
 * An edge is created for cards placed side by side.
 *
 * <p>
 * Graph representation:
 * - The cards are the vertices.
 * - Two cards next to each other have an edge.
 * - The weight is the novelty of the data.
 *
 * @author Grzegorz Ziemski
 */
@Entity(tableName = "FileSync_CardEdge")
public class CardEdge {

    public static final String STATUS_UNCHANGED = "UNCHANGED";

    public static final String STATUS_IMPORTED_BOTH_OLDER = "IMPORTED_BOTH_OLDER";

    public static final String STATUS_DECK_BOTH_OLDER = "DECK_BOTH_OLDER";

    public static final String STATUS_IMPORTED_ONE_NEWER = "IMPORTED_ONE_NEWER";

    public static final String STATUS_DECK_ONE_NEWER = "DECK_ONE_NEWER";

    public static final String STATUS_IMPORTED_BOTH_NEWER = "IMPORTED_BOTH_NEWER";

    public static final String STATUS_DECK_BOTH_NEWER = "DECK_BOTH_NEWER";

    public static final String STATUS_IMPORTED_BOTH_NEW = "IMPORTED_BOTH_NEW";

    public static final String STATUS_DECK_BOTH_NEW = "DECK_BOTH_NEW";

    public static final String STATUS_IMPORTED_FIRST_NEW = "IMPORTED_FIRST_NEW";

    public static final String STATUS_IMPORTED_SECOND_NEW = "IMPORTED_SECOND_NEW";

    public static final String STATUS_DECK_FIRST_NEW = "DECK_FIRST_NEW";

    public static final String STATUS_DECK_SECOND_NEW = "DECK_SECOND_NEW";

    @PrimaryKey
    private Integer id;

    private int fromCardImportedId;

    private int toCardImportedId;

    private String status;

    private int weight;

    private boolean deleted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getFromCardImportedId() {
        return fromCardImportedId;
    }

    public void setFromCardImportedId(int fromCardImportedId) {
        this.fromCardImportedId = fromCardImportedId;
    }

    public int getToCardImportedId() {
        return toCardImportedId;
    }

    public void setToCardImportedId(int toCardImportedId) {
        this.toCardImportedId = toCardImportedId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
