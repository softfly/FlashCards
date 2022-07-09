package pl.softfly.flashcards;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.Date;

import pl.softfly.flashcards.entity.deck.CardLearningProgress;

/**
 * Available grades:
 * 1) Again
 *    If the card is forgotten, the {@link CardLearningProgress#setInterval(Integer)} is divided by 2.
 * 2) Quick
 *    The quick can only be used, only the card has never been remembered yet.
 * 3) Hard
 *    The interval is multiplied by 150%
 * 4) Easy
 *    The interval is multiplied by 200%
 *
 * @author Grzegorz Ziemski
 */
public class CardReplayScheduler {

    /**
     * If again was selected first, the remaining grades will have 1 day.
     * 2 days * {@link CardReplayScheduler#DECREASE_IF_FORGOTTEN_PERCENT} = 1 day
     */
    private static final int AGAIN_FIRST_INTERVAL_MINUTES = 2 * 24 * 60;

    private static final int QUICK_FIRST_INTERVAL_MINUTES = 5;

    /**
     * 2 days * {@link CardReplayScheduler#HARD_FACTOR_PERCENT} = 3 days
     */
    private static final int HARD_FIRST_INTERVAL_MINUTES = 2 * 24 * 60;

    /**
     * 2.5 days * {@link CardReplayScheduler#EASY_FACTOR_PERCENT} = 5 days
     */
    private static final int EASY_FIRST_INTERVAL_MINUTES = 60 * 60;

    private static final int QUICK_FACTOR_PERCENT = 100;

    private static final int HARD_FACTOR_PERCENT = 150;

    private static final int EASY_FACTOR_PERCENT = 200;

    private static final int DECREASE_IF_FORGOTTEN_PERCENT = 50;

    @NonNull
    public CardLearningProgress scheduleFirstReplayAfterAgain(int cardId) {
        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setCardId(cardId);
        newLearningProgress.setInterval(AGAIN_FIRST_INTERVAL_MINUTES);
        newLearningProgress.setRemembered(false);
        return newLearningProgress;
    }

    /**
     * The quick can be used only when the card has never been remembered yet.
     */
    @NonNull
    public CardLearningProgress scheduleFirstReplayAfterQuick(int cardId) {
        return scheduleFirstReplay(
                cardId,
                QUICK_FIRST_INTERVAL_MINUTES,
                QUICK_FACTOR_PERCENT
        );
    }

    @NonNull
    public CardLearningProgress scheduleFirstReplayAfterEasy(int cardId) {
        return scheduleFirstReplay(
                cardId,
                EASY_FIRST_INTERVAL_MINUTES,
                EASY_FACTOR_PERCENT
        );
    }

    @NonNull
    public CardLearningProgress scheduleFirstReplayAfterHard(int cardId) {
        return scheduleFirstReplay(
                cardId,
                HARD_FIRST_INTERVAL_MINUTES,
                HARD_FACTOR_PERCENT
        );
    }

    @NonNull
    protected CardLearningProgress scheduleFirstReplay(
            int cardId,
            int defaultStartInterval,
            int factor
    ) {
        int newInterval = calcNewInterval(defaultStartInterval, factor);

        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setCardId(cardId);
        newLearningProgress.setInterval(newInterval);
        newLearningProgress.setNextReplayAt(calcNewReplayAt(newInterval));
        newLearningProgress.setRemembered(true);
        return newLearningProgress;
    }

    /**
     * The quick can only be used, only the card has never been remembered yet.
     * This is for a scenario where Again was previously chosen.
     */
    @NonNull
    public CardLearningProgress scheduleFirstReplayAfterQuick(CardLearningProgress learningProgress) {
        if (
                learningProgress.getRemembered().equals(true)
                        || learningProgress.getNextReplayAt() != null
        ) {
            throw new RuntimeException("The quick can only be used, only the card has never been remembered yet.");
        }
        return cloneRemembered(learningProgress, QUICK_FIRST_INTERVAL_MINUTES);
    }

    /**
     * Update only {@link CardLearningProgress#setRemembered(Boolean)} to false.
     */
    @NonNull
    public CardLearningProgress scheduleNextReplayAfterAgain(CardLearningProgress learningProgress) {
        CardLearningProgress newLearningProgress = cloneWithIds(learningProgress);
        newLearningProgress.setInterval(learningProgress.getInterval());
        newLearningProgress.setNextReplayAt(learningProgress.getNextReplayAt());
        newLearningProgress.setRemembered(false);
        return newLearningProgress;
    }

    @NonNull
    public CardLearningProgress scheduleNextReplayAfterEasy(@NonNull CardLearningProgress learningProgress) {
        return scheduleNextReplay(
                learningProgress,
                EASY_FACTOR_PERCENT
        );
    }

    @NonNull
    public CardLearningProgress scheduleNextReplayAfterHard(@NonNull CardLearningProgress learningProgress) {
        return scheduleNextReplay(
                learningProgress,
                HARD_FACTOR_PERCENT
        );
    }

    @NonNull
    protected CardLearningProgress scheduleNextReplay(
            @NonNull CardLearningProgress learningProgress,
            int factor
    ) {
        int newInterval;
        if (learningProgress.getRemembered()) {
            newInterval = calcNewInterval(learningProgress.getInterval(), factor);
        } else {
            newInterval = calcNewIntervalForgotten(learningProgress);
        }
        return cloneRemembered(learningProgress, newInterval);
    }

    /**
     * If the card is forgotten,
     * the {@link CardLearningProgress#setInterval(Integer)} is divided by 2.
     */
    protected int calcNewIntervalForgotten(CardLearningProgress learningProgress) {
        return Math.max(
                QUICK_FIRST_INTERVAL_MINUTES,
                (int) (learningProgress.getInterval() * (DECREASE_IF_FORGOTTEN_PERCENT / 100f))
        );
    }


    /* -----------------------------------------------------------------------------------------
     * Helpers
     * ----------------------------------------------------------------------------------------- */

    protected CardLearningProgress cloneWithIds(CardLearningProgress learningProgress) {
        CardLearningProgress newLearningProgress = new CardLearningProgress();
        newLearningProgress.setId(learningProgress.getId());
        newLearningProgress.setCardId(learningProgress.getCardId());
        return newLearningProgress;
    }

    protected CardLearningProgress cloneRemembered(
            CardLearningProgress learningProgress,
            int interval
    ) {
        CardLearningProgress newLearningProgress = cloneWithIds(learningProgress);
        newLearningProgress.setInterval(interval);
        newLearningProgress.setNextReplayAt(calcNewReplayAt(interval));
        newLearningProgress.setRemembered(true);
        return newLearningProgress;
    }

    protected int calcNewInterval(int previousInterval, int factor) {
        return (int) (previousInterval * (factor / 100f));
    }

    protected Date calcNewReplayAt(int minutes) {
        return Date.from(ZonedDateTime.now().plusMinutes(minutes).toInstant());
    }
}
