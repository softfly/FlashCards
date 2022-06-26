package pl.softfly.flashcards.entity.filesync;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a synchronized file.
 *
 * @author Grzegorz Ziemski
 */
@Entity(
        tableName = "FileSync_FileSynced",
        indices = {
                @Index(value = "uri", unique = true)
        }
)
public class FileSynced {

    @PrimaryKey
    private Integer id;

    private String uri;

    private Long lastSyncAt;

    /**
     * When was the deck recently updated.
     */
    private Long deckModifiedAt;

    /**
     * A deck can only automatically sync with one file.
     */
    private boolean autoSync;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(Long lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public Long getDeckModifiedAt() {
        return deckModifiedAt;
    }

    public void setDeckModifiedAt(Long deckModifiedAt) {
        this.deckModifiedAt = deckModifiedAt;
    }

    public boolean isAutoSync() {
        return autoSync;
    }

    public void setAutoSync(boolean autoSync) {
        this.autoSync = autoSync;
    }
}
