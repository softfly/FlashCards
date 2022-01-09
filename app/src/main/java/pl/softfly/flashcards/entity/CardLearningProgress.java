package pl.softfly.flashcards.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.util.Date;


@Entity
public class CardLearningProgress {

    private final static int DIVIDE_HOURS_TO_DAYS = 24;

    private Boolean remembered;

    private Integer interval;

    private Date nextReplayAt;

    @NonNull
    public Float getIntervalHour() {
        return interval / (float) DIVIDE_HOURS_TO_DAYS;
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
