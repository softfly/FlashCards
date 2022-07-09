package pl.softfly.flashcards.ui.card.study;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ListIterator;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CardReplayScheduler;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.entity.deck.CardLearningProgress;

/**
 * @author Grzegorz Ziemski
 */
public class StudyCardViewModel extends ViewModel {

    private final CardReplayScheduler cardReplayScheduler = new CardReplayScheduler();
    private final MutableLiveData<Card> card = new MutableLiveData<>();
    private final MutableLiveData<CardLearningProgress> againLearningProgress = new MutableLiveData<>();
    private final MutableLiveData<CardLearningProgress> quickLearningProgress = new MutableLiveData<>();
    private final MutableLiveData<CardLearningProgress> easyLearningProgress = new MutableLiveData<>();
    private final MutableLiveData<CardLearningProgress> hardLearningProgress = new MutableLiveData<>();
    private DeckDatabase deckDb;
    private AppDatabase appDb;
    private ListIterator<Card> cardIterator;

    public void nextCard(Consumer<? super Throwable> onError) {
        if (Objects.nonNull(cardIterator) && cardIterator.hasNext()) {
            setCard(cardIterator.next(), onError);
        } else {
            deckDb.cardDaoAsync().getNextCardsToReplay()
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess(cards -> {
                        cardIterator = cards.listIterator();
                        if (cardIterator.hasNext()) {
                            setCard(cardIterator.next(), onError);
                        } else {
                            setCard(null, onError);
                        }
                    })
                    .subscribe(cards -> {}, onError);
        }
    }

    protected void setCard(Card card, Consumer<? super Throwable> onError) {
        if (card == null) {
            this.card.postValue(null);
            return;
        }
        this.card.postValue(card);
        setupLearningProgress(card.getId(), onError);
    }

    @NonNull
    public Completable deleteCard(Consumer<? super Throwable> onError) {
        return deckDb.cardDao().deleteAsync(card.getValue())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    cardIterator.remove();
                    nextCard(onError);
                });
    }

    @NonNull
    public Completable revertCard(@NonNull Card card, Consumer<? super Throwable> onError) {
        return deckDb.cardDao().restoreAsync(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                            cardIterator.previous();
                            cardIterator.add(card);
                            cardIterator.previous();
                            nextCard(onError);
                        }
                );
    }

    private void setupLearningProgress(@Nullable Integer cardId, Consumer<? super Throwable> onError) {
        if (cardId == null) {
            //DEBUG this
            againLearningProgress.postValue(null);
            quickLearningProgress.postValue(null);
            easyLearningProgress.postValue(null);
            hardLearningProgress.postValue(null);
        }
        deckDb.cardLearningProgressAsyncDao()
                .findByCardId(cardId)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(this::scheduleNextReplay)
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        scheduleFirstReplay(cardId);
                    }
                })
                .subscribe(learningProgress -> {}, onError);
    }

    /**
     * No grade button has ever been selected for this card yet.
     */
    protected void scheduleFirstReplay(Integer cardId) {
        againLearningProgress.postValue(cardReplayScheduler.scheduleFirstReplayAfterAgain(cardId));
        quickLearningProgress.postValue(cardReplayScheduler.scheduleFirstReplayAfterQuick(cardId));
        easyLearningProgress.postValue(cardReplayScheduler.scheduleFirstReplayAfterEasy(cardId));
        hardLearningProgress.postValue(cardReplayScheduler.scheduleFirstReplayAfterHard(cardId));
    }

    /**
     * At least once, the grade button has been selected previously for this card.
     */
    protected void scheduleNextReplay(CardLearningProgress learningProgress) {
        againLearningProgress.postValue(cardReplayScheduler.scheduleNextReplayAfterAgain(learningProgress));
        scheduleNextReplayAfterQuick(learningProgress);
        easyLearningProgress.postValue(cardReplayScheduler.scheduleNextReplayAfterEasy(learningProgress));
        hardLearningProgress.postValue(cardReplayScheduler.scheduleNextReplayAfterHard(learningProgress));
    }

    /**
     * The quick can only be used, only the card has never been remembered yet.
     * This is for a scenario where Again was previously chosen.
     */
    protected void scheduleNextReplayAfterQuick(CardLearningProgress learningProgress) {
        if (
                learningProgress.getRemembered().equals(false)
                        && learningProgress.getNextReplayAt() == null
        ) {
            quickLearningProgress.postValue(cardReplayScheduler.scheduleFirstReplayAfterQuick(learningProgress));
        } else {
            quickLearningProgress.postValue(null);
        }
    }

    @NonNull
    public Completable updateLearningProgress(
            @NonNull CardLearningProgress learningProgress,
            String deckDbPath,
            Consumer<? super Throwable> onError
    ) {
        Completable updateCardLearningProgress;
        if (learningProgress.getId() == null) {
            updateCardLearningProgress = deckDb
                    .cardLearningProgressAsyncDao()
                    .insertAll(learningProgress);
        } else {
            updateCardLearningProgress = deckDb
                    .cardLearningProgressAsyncDao()
                    .updateAll(learningProgress);
        }
        return updateCardLearningProgress
                .andThen(appDb.deckDaoAsync().refreshLastUpdatedAt(deckDbPath))
                .doOnSuccess(deck -> nextCard(onError))
                .subscribeOn(Schedulers.io())
                .flatMapCompletable(deck -> Completable.complete());
    }

    @NonNull
    public MutableLiveData<Card> getCard() {
        return card;
    }

    @NonNull
    public MutableLiveData<CardLearningProgress> getAgainLearningProgress() {
        return againLearningProgress;
    }

    public MutableLiveData<CardLearningProgress> getQuickLearningProgress() {
        return quickLearningProgress;
    }

    @NonNull
    public MutableLiveData<CardLearningProgress> getEasyLearningProgress() {
        return easyLearningProgress;
    }

    @NonNull
    public MutableLiveData<CardLearningProgress> getHardLearningProgress() {
        return hardLearningProgress;
    }

    public void setDeckDb(DeckDatabase deckDb) {
        this.deckDb = deckDb;
    }

    public void setAppDb(AppDatabase appDb) {
        this.appDb = appDb;
    }
}
