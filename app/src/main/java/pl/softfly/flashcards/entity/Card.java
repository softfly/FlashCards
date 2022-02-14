package pl.softfly.flashcards.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

/**
 * The card in the deck.
 *
 * @author Grzegorz Ziemski
 */
@Entity(tableName = "Core_Card")
public class Card {

    @PrimaryKey
    private Integer id;

    private Integer ordinal;

    private String term;

    private String definition;

    private LocalDateTime modifiedAt;

    private LocalDateTime deletedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Card) {
            return id.equals(((Card) obj).getId());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
