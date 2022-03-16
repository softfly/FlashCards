package pl.softfly.flashcards;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

import pl.softfly.flashcards.entity.CardLearningProgress;

public class CardReplayScheduler {

    /**
     * Hours
     * 1 day multiply 24 hours
     */
    private static final int DEFAULT_AGAIN_FIRST_INTERVAL = 24;

    /**
     * Hours
     * 2.5 day multiply 24 hours
     */
    private static final int DEFAULT_HARD_FIRST_INTERVAL = 48;

    /**
     * Hours
     * 2.5 day multiply 24 hours
     */
    private static final int DEFAULT_EASY_FIRST_INTERVAL = 60;

    /**
     * Divide by 100 before use.
     */
    private static final int DEFAULT_HARD_FACTOR = 50;

    /**
     * Divide by 100 before use.
     */
    private static final int DEFAULT_EASY_FACTOR = 100;

    @NonNull
    public CardLearningProgress scheduleReplayAfterAgain() {
        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setInterval(DEFAULT_AGAIN_FIRST_INTERVAL);
        newLearningProgress.setRemembered(false);
        return newLearningProgress;
    }

    @NonNull
    public CardLearningProgress scheduleReplayAfterHard(CardLearningProgress learningProgress) {
        return scheduleReplayRemembered(learningProgress, DEFAULT_HARD_FIRST_INTERVAL, DEFAULT_HARD_FACTOR);
    }

    @NonNull
    public CardLearningProgress scheduleReplayAfterEasy(CardLearningProgress learningProgress) {
        return scheduleReplayRemembered(learningProgress, DEFAULT_EASY_FIRST_INTERVAL, DEFAULT_EASY_FACTOR);
    }

    @NonNull
    protected CardLearningProgress scheduleReplayRemembered(CardLearningProgress learningProgress, int defaultIntervalStart, int factor) {
        if (Objects.nonNull(learningProgress) && !learningProgress.getRemembered()) {
            return this.scheduleReplayForgotten();
        } else {
            int lastInterval = Objects.nonNull(learningProgress) && Objects.nonNull(learningProgress.getInterval()) ?
                    learningProgress.getInterval() : defaultIntervalStart;
            int newInterval = lastInterval * factor / 100 + lastInterval;
            Date nextReplayAt = Date.from(ZonedDateTime.now().plusHours(newInterval).toInstant());

            CardLearningProgress newLearningProgress = new CardLearningProgress();
            newLearningProgress.setInterval(newInterval);
            newLearningProgress.setNextReplayAt(nextReplayAt);
            newLearningProgress.setRemembered(true);
            return newLearningProgress;
        }
    }

    @NonNull
    protected CardLearningProgress scheduleReplayForgotten() {
        int newInterval = DEFAULT_AGAIN_FIRST_INTERVAL;
        Date nextReplayAt = Date.from(ZonedDateTime.now().plusHours(newInterval).toInstant());
        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setInterval(newInterval);
        newLearningProgress.setNextReplayAt(nextReplayAt);
        newLearningProgress.setRemembered(false);
        return newLearningProgress;
    }
}
