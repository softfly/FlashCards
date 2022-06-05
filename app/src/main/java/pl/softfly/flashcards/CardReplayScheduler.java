package pl.softfly.flashcards;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

import pl.softfly.flashcards.entity.CardLearningProgress;

/**
 * @author Grzegorz Ziemski
 */
public class CardReplayScheduler {

    /**
     * 1 day
     */
    private static final int DEFAULT_AGAIN_FIRST_INTERVAL_HOURS = 24;

    /**
     * 2 days
     */
    private static final int DEFAULT_HARD_FIRST_INTERVAL_HOURS = 48;

    /**
     * 2.5 days
     */
    private static final int DEFAULT_EASY_FIRST_INTERVAL_HOURS = 60;

    private static final int DEFAULT_HARD_FACTOR_PERCENT = 50;

    private static final int DEFAULT_EASY_FACTOR_PERCENT = 100;

    @NonNull
    public CardLearningProgress scheduleFirstReplayAfterAgain(int cardId) {
        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setCardId(cardId);
        newLearningProgress.setInterval(DEFAULT_AGAIN_FIRST_INTERVAL_HOURS);
        newLearningProgress.setRemembered(false);
        return newLearningProgress;
    }

    @NonNull
    public CardLearningProgress scheduleNextReplayAfterAgain(CardLearningProgress learningProgress) {
        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setId(learningProgress.getId());
        newLearningProgress.setCardId(learningProgress.getCardId());
        newLearningProgress.setInterval(DEFAULT_AGAIN_FIRST_INTERVAL_HOURS);
        newLearningProgress.setRemembered(false);
        return newLearningProgress;
    }

    @NonNull
    public CardLearningProgress scheduleFirstReplayAfterEasy(int cardId) {
        return scheduleFirstReplayRemembered(
                cardId,
                DEFAULT_EASY_FIRST_INTERVAL_HOURS,
                DEFAULT_EASY_FACTOR_PERCENT
        );
    }

    @NonNull
    public CardLearningProgress scheduleNextReplayAfterEasy(@NonNull CardLearningProgress learningProgress) {
        return scheduleNextReplay(
                learningProgress,
                DEFAULT_EASY_FACTOR_PERCENT
        );
    }

    @NonNull
    public CardLearningProgress scheduleFirstReplayAfterHard(int cardId) {
        return scheduleFirstReplayRemembered(
                cardId,
                DEFAULT_HARD_FIRST_INTERVAL_HOURS,
                DEFAULT_HARD_FACTOR_PERCENT
        );
    }

    @NonNull
    public CardLearningProgress scheduleNextReplayAfterHard(@NonNull CardLearningProgress learningProgress) {
        return scheduleNextReplay(
                learningProgress,
                DEFAULT_HARD_FACTOR_PERCENT
        );
    }

    @NonNull
    protected CardLearningProgress scheduleNextReplay(
            @NonNull CardLearningProgress learningProgress,
            int factor
    ) {
        return learningProgress.getRemembered() ?
                this.scheduleNextReplayAfterRemembered(learningProgress, factor) :
                this.scheduleNextReplayAfterForgotten(learningProgress);
    }

    /**
     * Start repeating from scratch if you forget it.
     * TODO The interval should be higher when you start over
     */
    @NonNull
    protected CardLearningProgress scheduleNextReplayAfterForgotten(CardLearningProgress learningProgress) {
        int newInterval = DEFAULT_AGAIN_FIRST_INTERVAL_HOURS;

        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setId(learningProgress.getId());
        newLearningProgress.setCardId(learningProgress.getCardId());
        newLearningProgress.setInterval(newInterval);
        newLearningProgress.setNextReplayAt(calcNewReplayAt(newInterval));
        newLearningProgress.setRemembered(false);
        return newLearningProgress;
    }

    @NonNull
    protected CardLearningProgress scheduleNextReplayAfterRemembered(
            @NonNull CardLearningProgress learningProgress,
            int factor
    ) {
        int newIntervalHours = calcNewIntervalHours(learningProgress.getInterval(), factor);

        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setId(learningProgress.getId());
        newLearningProgress.setCardId(learningProgress.getCardId());
        newLearningProgress.setInterval(newIntervalHours);
        newLearningProgress.setNextReplayAt(calcNewReplayAt(newIntervalHours));
        newLearningProgress.setRemembered(true);
        return newLearningProgress;
    }

    @NonNull
    protected CardLearningProgress scheduleFirstReplayRemembered(
            int cardId,
            int defaultStartIntervalHour,
            int factor
    ) {
        int newIntervalHours = calcNewIntervalHours(defaultStartIntervalHour, factor);

        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setCardId(cardId);
        newLearningProgress.setInterval(newIntervalHours);
        newLearningProgress.setNextReplayAt(calcNewReplayAt(newIntervalHours));
        newLearningProgress.setRemembered(true);
        return newLearningProgress;
    }

    protected int calcNewIntervalHours(int previousIntervalHour, int factor) {
        return previousIntervalHour * (factor / 100) + previousIntervalHour;
    }

    protected Date calcNewReplayAt(int intervalHour) {
        return Date.from(ZonedDateTime.now().plusHours(intervalHour).toInstant());
    }
}
