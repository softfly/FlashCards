package pl.softfly.flashcards.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author Grzegorz Ziemski
 */
@Entity
public class DeckConfig {

    public static final String FILE_SYNC_EDITING_BLOCKED_AT = "FileSync_EditingBlockedAt";

    public static final String STUDY_CARD_TERM_FONT_SIZE = "StudyCard_Term_FontSize";

    public static final String STUDY_CARD_DEFINITION_FONT_SIZE = "StudyCard_Term_FontSize";

    public static final String STUDY_CARD_TD_DISPLAY_RATIO = "StudyCard_TD_DisplayRatio";

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
