package pl.softfly.flashcards.entity.deck;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

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

    private Long createdAt;

    private Long modifiedAt;

    private Long deletedAt;

    private Long fileSyncCreatedAt;

    private Long fileSyncModifiedAt;

    private boolean termHtml;

    private boolean definitionHtml;

    private boolean disabled;

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

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Long deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getFileSyncCreatedAt() {
        return fileSyncCreatedAt;
    }

    public void setFileSyncCreatedAt(Long fileSyncCreatedAt) {
        this.fileSyncCreatedAt = fileSyncCreatedAt;
    }

    public Long getFileSyncModifiedAt() {
        return fileSyncModifiedAt;
    }

    public void setFileSyncModifiedAt(Long fileSyncModifiedAt) {
        this.fileSyncModifiedAt = fileSyncModifiedAt;
    }

    public boolean isTermHtml() {
        return termHtml;
    }

    public void setTermHtml(boolean termHtml) {
        this.termHtml = termHtml;
    }

    public boolean isDefinitionHtml() {
        return definitionHtml;
    }

    public void setDefinitionHtml(boolean definitionHtml) {
        this.definitionHtml = definitionHtml;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
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
