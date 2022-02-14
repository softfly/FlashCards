package pl.softfly.flashcards.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(tableName = "Core_Card_LearningProgress")
public class CardLearningProgress {

    private final static int DIVIDE_HOURS_TO_DAYS = 24;

    @PrimaryKey
    private Integer id;

    private int cardId;

    private Boolean remembered;

    private Integer interval;

    private Date nextReplayAt;

    @NonNull
    public Float getIntervalHour() {
        return interval / (float) DIVIDE_HOURS_TO_DAYS;
    }

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
