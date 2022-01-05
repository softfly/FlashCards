package pl.softfly.flashcards.entity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

/**
 * The card in the deck.
 */
@Entity(tableName = "Core_Card")
public class Card {

    @PrimaryKey
    private Integer id;

    private Integer ordinal;

    private String question;

    private String answer;

    private LocalDateTime modifiedAt;

    private LocalDateTime deletedAt;

    @Embedded
    CardLearningProgress learningProgress;

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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
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

    public CardLearningProgress getLearningProgress() {
        return learningProgress;
    }

    public void setLearningProgress(CardLearningProgress learningProgress) {
        this.learningProgress = learningProgress;
    }
}
