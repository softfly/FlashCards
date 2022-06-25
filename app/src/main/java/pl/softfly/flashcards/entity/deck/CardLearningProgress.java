package pl.softfly.flashcards.entity.deck;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Grzegorz Ziemski
 */
@Entity(tableName = "Core_CardLearningProgress",
        indices = {
                @Index(value = "cardId", unique = true)
        })
public class CardLearningProgress {

    @PrimaryKey
    private Integer id;

    private int cardId;

    /**
     * Status if the user currently remembers this card.
     */
    private Boolean remembered;

    /**
     * Hours added to the next repeat if the user remembered the card.
     */
    private Integer interval;

    private Date nextReplayAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public Boolean getRemembered() {
        return remembered;
    }

    public void setRemembered(Boolean remembered) {
        this.remembered = remembered;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Date getNextReplayAt() {
        return nextReplayAt;
    }

    public void setNextReplayAt(Date nextReplayAt) {
        this.nextReplayAt = nextReplayAt;
    }
}
