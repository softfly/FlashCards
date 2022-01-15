package pl.softfly.flashcards.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author Grzegorz Ziemski
 */
@Entity
public class DeckConfig {

    public static final String FILE_SYNC_EDITING_BLOCKED_AT = "FileSync_EditingBlockedAt";

    @PrimaryKey
    private Integer id;

    private String key;

    private String value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
