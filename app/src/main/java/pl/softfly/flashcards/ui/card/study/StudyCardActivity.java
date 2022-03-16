package pl.softfly.flashcards.ui.card.study;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.android.material.snackbar.Snackbar;

import java.util.ListIterator;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CardReplayScheduler;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.HtmlUtil;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.entity.CardLearningProgress;
import pl.softfly.flashcards.entity.DeckConfig;
import pl.softfly.flashcards.ui.IconWithTextInTopbarActivity;
import pl.softfly.flashcards.ui.card.EditCardActivity;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

public abstract class StudyCardActivity extends IconWithTextInTopbarActivity {

    private final static int DEFAULT_TERM_FONT_SIZE = 24;
    private final static int DEFAULT_DEFINITION_FONT_SIZE = 24;
    private final CardReplayScheduler cardReplayScheduler = new CardReplayScheduler();
    private final HtmlUtil htmlUtil = HtmlUtil.getInstance();
    protected DeckDatabase deckDb;
    private ListIterator<Card> cardIterator;
    private LiveData<Card> card;
    private String deckName;
    private CardLearningProgress againLearningProgress;
    private CardLearningProgress easyLearningProgress;
    private CardLearningProgress hardLearningProgress;
    private ZoomTextView termView;
    private ZoomTextView definitionView;
    private TextView showDefinitionView;
    private Button againButton;
    private Button easyButton;
    private Button hardButton;
    private View gradeButtonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_card);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DeckRecyclerViewAdapter.DECK_NAME);
        deckDb = AppDatabaseUtil.getInstance(getApplicationContext()).getDeckDatabase(deckName);
        loadNextCard();

        gradeButtonsLayout = findViewById(R.id.gradeButtonsLayout);
        gradeButtonsLayout.setVisibility(INVISIBLE);
        initTermView();
        initShowDefinition();
        initDefinitionView();
        initAgainButton();
        initEasyButton();
        initHardButton();
    }

    private void initShowDefinition() {
        showDefinitionView = findViewById(R.id.showDefinitionView);
        showDefinitionView.setOnClickListener(v -> {
            if (htmlUtil.isHtml(card.getValue().getDefinition())) {
                definitionView.setText(htmlUtil.fromHtml(card.getValue().getDefinition()));
            } else {
                definitionView.setText(card.getValue().getDefinition());
            }
            gradeButtonsLayout.setVisibility(VISIBLE);
            definitionView.setVisibility(VISIBLE);
            showDefinitionView.setVisibility(INVISIBLE);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("ClickableViewAccessibility")
    protected void initTermView() {
        termView = findViewById(R.id.termView);
        termView.setMovementMethod(new ScrollingMovementMethod());
        deckDb.deckConfigAsyncDao().getFloatByKey(DeckConfig.STUDY_CARD_TERM_FONT_SIZE)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(deckConfig -> termView.setTextSize(deckConfig))
                .subscribe(deckConfig -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        StudyCardActivity.class.getSimpleName() + "_InitTermView",
                        "Error while read the deck view settings."
                ));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void initDefinitionView() {
        definitionView = findViewById(R.id.definitionView);
        definitionView.setMovementMethod(new ScrollingMovementMethod());
        definitionView.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(VISIBLE);
            if (htmlUtil.isHtml(card.getValue().getDefinition())) {
                definitionView.setText(htmlUtil.fromHtml(card.getValue().getDefinition()));
            } else {
                definitionView.setText(card.getValue().getDefinition());
            }
        });
        deckDb.deckConfigAsyncDao().getFloatByKey(DeckConfig.STUDY_CARD_DEFINITION_FONT_SIZE)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(deckConfig -> definitionView.setTextSize(deckConfig))
                .subscribe(deckConfig -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        StudyCardActivity.class.getSimpleName() + "_InitDefinitionView",
                        "Error while read the deck view settings."
                ));
    }

    protected void initAgainButton() {
        againButton = findViewById(R.id.againButton);
        againButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            definitionView.setVisibility(INVISIBLE);
            showDefinitionView.setVisibility(VISIBLE);

            updateCardLearningProgress(againLearningProgress);
        });
    }

    private void initEasyButton() {
        easyButton = findViewById(R.id.easyButton);
        easyButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            definitionView.setVisibility(INVISIBLE);
            showDefinitionView.setVisibility(VISIBLE);
            updateCardLearningProgress(easyLearningProgress);
        });
    }

    private void initHardButton() {
        hardButton = findViewById(R.id.hardButton);
        hardButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            definitionView.setVisibility(INVISIBLE);
            showDefinitionView.setVisibility(VISIBLE);
            updateCardLearningProgress(hardLearningProgress);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void updateCardLearningProgress(@NonNull CardLearningProgress learningProgress) {
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
        updateCardLearningProgress.subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(this::loadNextCard))
                .subscribe(() -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        StudyCardActivity.class.getSimpleName() + "_UpdateCardLearningProgress",
                        "Error while updating card learning progress."
                ));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadNextCard() {
        if (Objects.nonNull(cardIterator) && cardIterator.hasNext()) {
            displayCard(cardIterator.next().getId());
        } else {
            deckDb.cardDaoAsync().getNextCards()
                    .subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .doOnSuccess(cards -> runOnUiThread(() -> {
                        cardIterator = cards.listIterator();
                        if (cardIterator.hasNext()) {
                            displayCard(cardIterator.next().getId());
                        } else {
                            NoStudyCardsDialog dialog = new NoStudyCardsDialog();
                            dialog.show(this.getSupportFragmentManager(), "NoStudyCardsDialog");
                        }
                    }))
                    .subscribe(cards -> {
                    }, e -> getExceptionHandler().handleException(
                            e, getSupportFragmentManager(),
                            StudyCardActivity.class.getSimpleName() + "_LoadNextCard",
                            "Error while loading the next card."
                    ));
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void displayCard(int cardId) {
        runOnUiThread(() -> {
            if (card != null) {
                card.removeObservers(this);
            }
            card = deckDb.cardDaoLiveData().getCard(cardId);
            card.observe(this, card -> {
                if (card.getDeletedAt() == null) {
                    if (htmlUtil.isHtml(card.getTerm())) {
                        termView.setText(htmlUtil.fromHtml(card.getTerm()));
                    } else {
                        termView.setText(card.getTerm());
                    }
                }
            });
        });
        deckDb.cardLearningProgressAsyncDao()
                .findByCardId(cardId)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(cardLearningProgress -> {
                    againLearningProgress = cardReplayScheduler.scheduleReplayAfterAgain();
                    easyLearningProgress = cardReplayScheduler.scheduleReplayAfterEasy(cardLearningProgress);
                    hardLearningProgress = cardReplayScheduler.scheduleReplayAfterHard(cardLearningProgress);

                    runOnUiThread(() -> {
                        againButton.setText("Again\n" + againLearningProgress.getIntervalHour());
                        easyButton.setText("Easy\n" + easyLearningProgress.getIntervalHour());
                        hardButton.setText("Hard\n" + hardLearningProgress.getIntervalHour());
                    });
                })
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        againLearningProgress = cardReplayScheduler.scheduleReplayAfterAgain();
                        easyLearningProgress = cardReplayScheduler.scheduleReplayAfterEasy(null);
                        hardLearningProgress = cardReplayScheduler.scheduleReplayAfterHard(null);

                        runOnUiThread(() -> {
                            againButton.setText("Again\n" + againLearningProgress.getIntervalHour());
                            easyButton.setText("Easy\n" + easyLearningProgress.getIntervalHour());
                            hardButton.setText("Hard\n" + hardLearningProgress.getIntervalHour());
                        });
                    }
                })
                .subscribe(cardLearningProgress -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        StudyCardActivity.class.getSimpleName() + "_DisplayCard",
                        "Error while displaying the card."
                ));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_study_card, menu);
        menu.add(0, R.id.edit, 1,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_round_edit_24),
                        "Edit"
                ));
        menu.add(0, R.id.delete, 2,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_round_delete_24),
                        "Delete"
                ));
        menu.add(0, R.id.resetView, 3,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_empty),
                        "Reset view"
                ));
        menu.add(0, R.id.rememberedForever, 4,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_baseline_done_24),
                        "Remembered forever"
                ));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            startEditCardActivity();
            return true;
        } else if (item.getItemId() == R.id.delete) {
            deleteCard(card.getValue());
        } else if (item.getItemId() == R.id.resetView) {
            resetView();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startEditCardActivity() {
        Intent intent = new Intent(this, EditCardActivity.class);
        intent.putExtra(EditCardActivity.DECK_NAME, deckName);
        intent.putExtra(EditCardActivity.CARD_ID, card.getValue().getId());
        this.startActivity(intent);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteCard(@NonNull Card card) {
        cardIterator.remove();
        deckDb.cardDao().deleteAsync(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    loadNextCard();
                    Snackbar.make(findViewById(android.R.id.content),
                            "The card has been deleted.",
                            Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> revertCard(card))
                            .show();
                })
                .subscribe(() -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        StudyCardActivity.class.getSimpleName() + "_OnClickDeleteCard",
                        "Error while removing the card."
                ));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void revertCard(@NonNull Card card) {
        deckDb.cardDao().restoreAsync(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(
                        () -> {
                            cardIterator.previous();
                            cardIterator.add(card);
                            cardIterator.previous();
                            loadNextCard();
                            Toast.makeText(
                                    this,
                                    "The card has been restored.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        })
                )
                .subscribe(() -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        StudyCardActivity.class.getSimpleName() + "_OnClickRevertCard",
                        "Error while restoring the card."
                ));
    }

    protected void resetView() {
        termView.setTextSize(DEFAULT_TERM_FONT_SIZE);
        definitionView.setTextSize(DEFAULT_DEFINITION_FONT_SIZE);
    }

    @Override
    protected void onStop() {
        deckDb.deckConfigAsyncDao().updateDeckConfig(
                DeckConfig.STUDY_CARD_TERM_FONT_SIZE,
                Float.toString(termView.getScaledTextSize()),
                getOnErrorSavingViewSettings()
        );
        deckDb.deckConfigAsyncDao().updateDeckConfig(
                DeckConfig.STUDY_CARD_DEFINITION_FONT_SIZE,
                Float.toString(definitionView.getScaledTextSize()),
                getOnErrorSavingViewSettings()
        );
        super.onStop();
    }

    @NonNull
    protected Consumer<? super Throwable> getOnErrorSavingViewSettings() {
        return e -> getExceptionHandler().handleException(
                e, getSupportFragmentManager(),
                StudyCardActivity.class.getSimpleName() + "_OnStop",
                "Error while saving deck view settings."
        );
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}