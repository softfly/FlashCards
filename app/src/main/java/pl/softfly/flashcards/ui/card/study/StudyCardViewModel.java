package pl.softfly.flashcards.ui.card.study;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ListIterator;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CardReplayScheduler;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.entity.CardLearningProgress;

/**
 * @author Grzegorz Ziemski
 */
public class StudyCardViewModel extends ViewModel {

    private final CardReplayScheduler cardReplayScheduler = new CardReplayScheduler();
    private final MutableLiveData<Card> card = new MutableLiveData<>();
    private final MutableLiveData<CardLearningProgress> againLearningProgress = new MutableLiveData<>();
    private final MutableLiveData<CardLearningProgress> easyLearningProgress = new MutableLiveData<>();
    private final MutableLiveData<CardLearningProgress> hardLearningProgress = new MutableLiveData<>();
    protected DeckDatabase deckDb;
    private ListIterator<Card> cardIterator;

    public void nextCard() {
        if (Objects.nonNull(cardIterator) && cardIterator.hasNext()) {
            Card card = cardIterator.next();
            this.card.postValue(card);
            setupLearningProgress(card.getId());
        } else {
            deckDb.cardDaoAsync().getNextCards()
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess(cards -> {
                        cardIterator = cards.listIterator();
                        if (cardIterator.hasNext()) {
                            Card card = cardIterator.next();
                            this.card.postValue(cardIterator.next());
                            setupLearningProgress(card.getId());
                        } else {
                            card.postValue(null);
                        }
                    })
                    .subscribe();
        }
    }

    @NonNull
    public Completable deleteCard() {
        return deckDb.cardDao().deleteAsync(card.getValue())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    cardIterator.remove();
                    nextCard();
                });
    }

    @NonNull
    public Completable revertCard(@NonNull Card card) {
        return deckDb.cardDao().restoreAsync(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                            cardIterator.previous();
                            cardIterator.add(card);
                            cardIterator.previous();
                            nextCard();
                        }
                );
    }

    private void setupLearningProgress(@Nullable Integer cardId) {
        if (cardId == null) {
            againLearningProgress.postValue(null);
            easyLearningProgress.postValue(null);
            hardLearningProgress.postValue(null);
        }
        deckDb.cardLearningProgressAsyncDao()
                .findByCardId(cardId)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(cardLearningProgress -> {
                    againLearningProgress.postValue(cardReplayScheduler.scheduleNextReplayAfterAgain(cardLearningProgress));
                    easyLearningProgress.postValue(cardReplayScheduler.scheduleNextReplayAfterEasy(cardLearningProgress));
                    hardLearningProgress.postValue(cardReplayScheduler.scheduleNextReplayAfterHard(cardLearningProgress));
                })
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        againLearningProgress.postValue(cardReplayScheduler.scheduleFirstReplayAfterAgain(cardId));
                        easyLearningProgress.postValue(cardReplayScheduler.scheduleFirstReplayAfterEasy(cardId));
                        hardLearningProgress.postValue(cardReplayScheduler.scheduleFirstReplayAfterHard(cardId));
                    }
                })
                .subscribe();
    }

    @NonNull
    public Completable updateLearningProgress(@NonNull CardLearningProgress learningProgress) {
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
        return updateCardLearningProgress.subscribeOn(Schedulers.io());
    }

    @NonNull
    public MutableLiveData<Card> getCard() {
        return card;
    }

    @NonNull
    public MutableLiveData<CardLearningProgress> getAgainLearningProgress() {
        return againLearningProgress;
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
}
