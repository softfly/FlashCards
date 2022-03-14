package pl.softfly.flashcards.ui.card.study;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CardReplayScheduler;
import pl.softfly.flashcards.HtmlUtil;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.entity.CardLearningProgress;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

public abstract class StudyCardActivity extends AppCompatActivity {

    private final static String TAG = "ViewCardActivity";
    private final CardReplayScheduler cardReplayScheduler = new CardReplayScheduler();
    private final HtmlUtil htmlUtil = HtmlUtil.getInstance();
    ListIterator<Card> cardIterator;
    private CardLearningProgress againLearningProgress;
    private CardLearningProgress easyLearningProgress;
    private CardLearningProgress hardLearningProgress;
    private String deckName;
    @Nullable
    private DeckDatabase deckDb;
    private Card card;
    private List<Card> cards;
    private TextView termView;
    private TextView definitionView;
    private Button againButton;
    private Button easyButton;
    private Button hardButton;
    private View gradeButtonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_view_card);

            Intent intent = getIntent();
            deckName = intent.getStringExtra(DeckRecyclerViewAdapter.DECK_NAME);
            deckDb = AppDatabaseUtil.getInstance(getApplicationContext()).getDeckDatabase(deckName);
            loadNextCard();

            gradeButtonsLayout = findViewById(R.id.gradeButtonsLayout);
            gradeButtonsLayout.setVisibility(INVISIBLE);
            initTermView();
            initDefinitionView();
            initAgainButton();
            initEasyButton();
            initHardButton();
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(getBaseContext())
                    .setTitle("Exception")
                    .setMessage(e.getMessage())
                    .setNeutralButton(android.R.string.ok, (dialog, which) -> onSupportNavigateUp())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(this.getSupportFragmentManager(), TAG);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initTermView() {
        termView = findViewById(R.id.termView);
        termView.setMovementMethod(new ScrollingMovementMethod());
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initDefinitionView() {
        definitionView = findViewById(R.id.definitionView);
        definitionView.setMovementMethod(new ScrollingMovementMethod());
        definitionView.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(VISIBLE);
            if (htmlUtil.isHtml(card.getDefinition())) {
                definitionView.setText(htmlUtil.fromHtml(card.getDefinition()));
            } else {
                definitionView.setText(card.getDefinition());
            }
        });
    }

    protected void initAgainButton() {
        againButton = findViewById(R.id.againButton);
        againButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            updateCard(againLearningProgress);
        });
    }

    protected void initEasyButton() {
        easyButton = findViewById(R.id.easyButton);
        easyButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            updateCard(easyLearningProgress);
        });
    }

    protected void initHardButton() {
        hardButton = findViewById(R.id.hardButton);
        hardButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            updateCard(hardLearningProgress);
        });
    }

    private void updateCard(@NonNull CardLearningProgress learningProgress) {
        deckDb.cardDaoAsync().updateAll(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    Completable c;
                    if (learningProgress.getId() == null) {
                        c = deckDb.cardLearningProgressAsyncDao()
                                .insertAll(learningProgress);
                    } else {
                        c = deckDb.cardLearningProgressAsyncDao()
                                .updateAll(learningProgress);
                    }
                    c.subscribeOn(Schedulers.io())
                            .doOnComplete(() -> runOnUiThread(this::loadNextCard))
                            .subscribe();
                })
                .subscribe();
    }

    protected void loadNextCard() {
        if (Objects.nonNull(cardIterator) && cardIterator.hasNext()) {
            displayCard(cardIterator.next());
        } else {
            deckDb.cardDaoAsync().getNextCards()
                    .subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .doOnSuccess(cards -> runOnUiThread(() -> {
                        this.cards = cards;
                        cardIterator = cards.listIterator();
                        if (cardIterator.hasNext()) {
                            displayCard(cardIterator.next());
                        } else {
                            NoStudyCardsDialog dialog = new NoStudyCardsDialog();
                            dialog.show(this.getSupportFragmentManager(), "NoStudyCardsDialog");
                        }
                    }))
                    .subscribe();
        }
    }

    protected void displayCard(@NonNull Card card) {
        this.card = card;
        if (htmlUtil.isHtml(card.getTerm())) {
            termView.setText(htmlUtil.fromHtml(card.getTerm()));
        } else {
            termView.setText(card.getTerm());
        }

        definitionView.setText("?\nShow definition");
        deckDb.cardLearningProgressAsyncDao()
                .findByCardId(card.getId())
                .subscribeOn(Schedulers.io())
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
                    againLearningProgress = cardReplayScheduler.scheduleReplayAfterAgain();
                    easyLearningProgress = cardReplayScheduler.scheduleReplayAfterEasy(cardLearningProgress);
                    hardLearningProgress = cardReplayScheduler.scheduleReplayAfterHard(cardLearningProgress);

                    runOnUiThread(() -> {
                        againButton.setText("Again\n" + againLearningProgress.getIntervalHour());
                        easyButton.setText("Easy\n" + easyLearningProgress.getIntervalHour());
                        hardButton.setText("Hard\n" + hardLearningProgress.getIntervalHour());
                    });
                }, Throwable::printStackTrace);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}