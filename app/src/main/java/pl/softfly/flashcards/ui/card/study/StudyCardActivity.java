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
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.HtmlUtil;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.entity.CardLearningProgress;
import pl.softfly.flashcards.entity.DeckConfig;
import pl.softfly.flashcards.ui.IconWithTextInTopbarActivity;
import pl.softfly.flashcards.ui.card.EditCardActivity;

public abstract class StudyCardActivity extends IconWithTextInTopbarActivity {

    public static final String DECK_DB_PATH = "deckDbPath";
    private final static float DIVIDE_HOURS_TO_DAYS = 24;
    private final static int DEFAULT_TERM_FONT_SIZE = 24;
    private final static int DEFAULT_DEFINITION_FONT_SIZE = 24;

    private final HtmlUtil htmlUtil = HtmlUtil.getInstance();
    protected DeckDatabase deckDb;
    protected AppDatabase appDb;
    private String deckDbPath;
    private StudyCardViewModel model;
    private ZoomTextView termView;
    private ZoomTextView definitionView;
    private TextView showDefinitionView;
    private View gradeButtonsLayout;
    private Button againButton;
    private Button easyButton;
    private Button hardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_card);

        Intent intent = getIntent();
        deckDbPath = intent.getStringExtra(DECK_DB_PATH);
        Objects.nonNull(deckDbPath);
        deckDb = AppDatabaseUtil.getInstance(getApplicationContext()).getDeckDatabase(deckDbPath);
        appDb = AppDatabaseUtil.getInstance(getApplicationContext()).getAppDatabase();

        model = new ViewModelProvider(this).get(StudyCardViewModel.class);
        model.setDeckDb(deckDb);

        gradeButtonsLayout = findViewById(R.id.gradeButtonsLayout);
        gradeButtonsLayout.setVisibility(INVISIBLE);
        initTermView();
        initShowDefinition();
        initDefinitionView();
        initAgainButton();
        initEasyButton();
        initHardButton();

        model.getCard().observe(this, this::onChanged);
        model.nextCard();
    }

    protected void onChanged(@Nullable Card card) {
        if (card == null) {
            NoStudyCardsDialog dialog = new NoStudyCardsDialog();
            dialog.show(getSupportFragmentManager(), "NoStudyCardsDialog");
        } else {
            if (htmlUtil.isHtml(card.getTerm())) {
                termView.setText(htmlUtil.fromHtml(card.getTerm()));
            } else {
                termView.setText(card.getTerm());
            }
            if (htmlUtil.isHtml(card.getDefinition())) {
                definitionView.setText(htmlUtil.fromHtml(card.getDefinition()));
            } else {
                definitionView.setText(card.getDefinition());
            }
            gradeButtonsLayout.setVisibility(INVISIBLE);
            definitionView.setVisibility(INVISIBLE);
            showDefinitionView.setVisibility(VISIBLE);
        }
    }

    private void initShowDefinition() {
        showDefinitionView = findViewById(R.id.showDefinitionView);
        showDefinitionView.setOnClickListener(v -> {
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
            updateCardLearningProgress(model.getAgainLearningProgress().getValue());
            model.nextCard();
        });
        model.getAgainLearningProgress().observe(this,
                cardLearningProgress ->
                        againButton.setText("Again\n" + cardLearningProgress.getInterval() / DIVIDE_HOURS_TO_DAYS ));
    }

    private void initEasyButton() {
        easyButton = findViewById(R.id.easyButton);
        easyButton.setOnClickListener(v -> {
            updateCardLearningProgress(model.getEasyLearningProgress().getValue());
            model.nextCard();
        });
        model.getEasyLearningProgress().observe(this,
                cardLearningProgress ->
                        easyButton.setText("Easy\n" + cardLearningProgress.getInterval() / DIVIDE_HOURS_TO_DAYS ));
    }

    private void initHardButton() {
        hardButton = findViewById(R.id.hardButton);
        hardButton.setOnClickListener(v -> {
            updateCardLearningProgress(model.getHardLearningProgress().getValue());
            model.nextCard();
        });
        model.getHardLearningProgress().observe(this,
                cardLearningProgress ->
                        hardButton.setText("Hard\n" + cardLearningProgress.getInterval() / DIVIDE_HOURS_TO_DAYS ));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void updateCardLearningProgress(@NonNull CardLearningProgress learningProgress) {
        model.updateLearningProgress(learningProgress)
                .subscribe(() -> refreshLastUpdatedAt()
                        , e -> getExceptionHandler().handleException(
                                e, getSupportFragmentManager(),
                                StudyCardActivity.class.getSimpleName() + "_UpdateCardLearningProgress",
                                "Error while updating card learning progress."
                        ));
    }

    protected void refreshLastUpdatedAt() {
        appDb.deckDaoAsync().refreshLastUpdatedAt(deckDbPath)
                .subscribe(deck -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        StudyCardActivity.class.getSimpleName() + "_UpdateCardLearningProgress",
                        "Error while updating card learning progress."
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            startEditCardActivity();
            return true;
        } else if (item.getItemId() == R.id.delete) {
            deleteCard();
        } else if (item.getItemId() == R.id.resetView) {
            resetView();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startEditCardActivity() {
        Intent intent = new Intent(this, EditCardActivity.class);
        intent.putExtra(EditCardActivity.DECK_DB_PATH, deckDbPath);
        intent.putExtra(EditCardActivity.CARD_ID, model.getCard().getValue().getId());
        this.startActivity(intent);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteCard() {
        Card card = model.getCard().getValue();
        model.deleteCard()
                .doOnComplete(() -> Snackbar.make(findViewById(android.R.id.content),
                                "The card has been deleted.",
                                Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> revertCard(card))
                        .show())
                .subscribe(() -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        StudyCardActivity.class.getSimpleName() + "_OnClickDeleteCard",
                        "Error while removing the card."
                ));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void revertCard(@NonNull Card card) {
        model.revertCard(card)
                .doOnComplete(() -> runOnUiThread(
                        () -> Toast.makeText(
                                this,
                                "The card has been restored.",
                                Toast.LENGTH_SHORT
                        ).show())
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
    protected String getDeckName(@NonNull String deckDbPath) {
        return deckDbPath.substring(deckDbPath.lastIndexOf("/") + 1);
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