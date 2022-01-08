package pl.softfly.flashcards.ui.card.study;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CardReplayScheduler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.entity.CardLearningProgress;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public abstract class StudyCardActivity extends AppCompatActivity {

    private final static String TAG = "ViewCardActivity";

    java.util.ListIterator<Card> cardIterator;
    CardLearningProgress againLearningProgress;
    CardLearningProgress easyLearningProgress;
    CardLearningProgress hardLearningProgress;
    private String deckName;
    private DeckDatabase deckDb;
    private Card card;
    private List<Card> cards;
    private TextView questionView;
    private TextView answerView;
    private Button againButton;
    private Button easyButton;
    private Button hardButton;
    private View gradeButtonsLayout;
    private final CardReplayScheduler cardReplayScheduler = new CardReplayScheduler();

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
            initQuestionView();
            initAnswerView();
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
    protected void initQuestionView() {
        questionView = findViewById(R.id.questionView);
        questionView.setMovementMethod(new ScrollingMovementMethod());
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initAnswerView() {
        answerView = findViewById(R.id.answerView);
        answerView.setMovementMethod(new ScrollingMovementMethod());
        answerView.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(VISIBLE);
            answerView.setText(card.getAnswer());
        });
    }

    protected void initAgainButton() {
        againButton = findViewById(R.id.againButton);
        againButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            card.setLearningProgress(againLearningProgress);

            deckDb.cardDaoAsync().updateAll(card)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete(() -> runOnUiThread(this::loadNextCard))
                    .subscribe();
        });
    }

    protected void initEasyButton() {
        easyButton = findViewById(R.id.easyButton);
        easyButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            card.setLearningProgress(easyLearningProgress);

            deckDb.cardDaoAsync().updateAll(card)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete(() -> runOnUiThread(this::loadNextCard))
                    .subscribe();
        });
    }

    protected void initHardButton() {
        hardButton = findViewById(R.id.hardButton);
        hardButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            card.setLearningProgress(hardLearningProgress);


            deckDb.cardDaoAsync().updateAll(card)
                    .subscribeOn(Schedulers.io())
                    .doOnComplete(() -> runOnUiThread(this::loadNextCard))
                    .subscribe();
            loadNextCard();
        });
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

    protected void displayCard(Card card) {
        this.card = card;
        questionView.setText(card.getQuestion());
        answerView.setText("?\nShow answer");

        againLearningProgress = cardReplayScheduler.scheduleReplayAfterAgain();
        againButton.setText("Again\n" + againLearningProgress.getIntervalHour());

        easyLearningProgress = cardReplayScheduler.scheduleReplayAfterEasy(card.getLearningProgress());
        easyButton.setText("Easy\n" + easyLearningProgress.getIntervalHour());

        hardLearningProgress = cardReplayScheduler.scheduleReplayAfterHard(card.getLearningProgress());
        hardButton.setText("Hard\n" + hardLearningProgress.getIntervalHour());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}