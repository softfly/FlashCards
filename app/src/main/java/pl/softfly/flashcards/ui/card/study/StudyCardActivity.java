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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.HtmlUtil;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.ActivityStudyCardBinding;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.entity.deck.CardLearningProgress;
import pl.softfly.flashcards.entity.deck.DeckConfig;
import pl.softfly.flashcards.ui.base.IconInTopbarActivity;
import pl.softfly.flashcards.ui.card.EditCardActivity;

public abstract class StudyCardActivity extends IconInTopbarActivity {

    public static final String DECK_DB_PATH = "deckDbPath";
    private final static float DIVIDE_MINUTES_TO_DAYS = 24 * 60;
    private final static int DEFAULT_TERM_FONT_SIZE = 24;
    private final static int DEFAULT_DEFINITION_FONT_SIZE = 24;

    private final HtmlUtil htmlUtil = HtmlUtil.getInstance();
    private ActivityStudyCardBinding binding;
    private DeckDatabase deckDb;
    private AppDatabase appDb;
    private String deckDbPath;
    private StudyCardViewModel model;

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudyCardBinding.inflate(getLayoutInflater());
        setContentView(getBinding().getRoot());

        Intent intent = getIntent();
        deckDbPath = intent.getStringExtra(DECK_DB_PATH);
        Objects.nonNull(deckDbPath);
        deckDb = getDeckDatabase(deckDbPath);
        appDb = getAppDatabase();

        model = new ViewModelProvider(this).get(StudyCardViewModel.class);
        model.setDeckDb(deckDb);

        initTermView();
        initDefinitionView();
        getBinding().showDefinitionView.setOnClickListener(this::onClickShowDefinition);

        getBinding().gradeButtonsLayout.setVisibility(INVISIBLE);
        getBinding().againButton.setOnClickListener(this::onAgainClick);
        model.getAgainLearningProgress().observe(this, this::onAgainChanged);
        getBinding().quickButton.setOnClickListener(this::onQuickClick);
        model.getQuickLearningProgress().observe(this, this::onQuickChanged);
        getBinding().easyButton.setOnClickListener(this::onEasyClick);
        model.getEasyLearningProgress().observe(this, this::onEasyChanged);
        getBinding().hardButton.setOnClickListener(this::onHardClick);
        model.getHardLearningProgress().observe(this, this::onHardChanged);

        model.getCard().observe(this, this::onCardChanged);
        model.nextCard();
    }

    protected void onCardChanged(@Nullable Card card) {
        if (card == null) {
            NoStudyCardsDialog dialog = new NoStudyCardsDialog();
            dialog.show(getSupportFragmentManager(), "NoStudyCardsDialog");
            return;
        }
        setText(getBinding().termView, card.getTerm());
        setText(getBinding().definitionView, card.getDefinition());
        getBinding().gradeButtonsLayout.setVisibility(INVISIBLE);
        getBinding().definitionView.setVisibility(INVISIBLE);
        getBinding().showDefinitionView.setVisibility(VISIBLE);
    }

    protected void setText(TextView textView, String value) {
        if (htmlUtil.isHtml(value)) {
            textView.setText(htmlUtil.fromHtml(value));
        } else {
            textView.setText(value);
        }
    }

    private void onClickShowDefinition(View v) {
        getBinding().gradeButtonsLayout.setVisibility(VISIBLE);
        getBinding().definitionView.setVisibility(VISIBLE);
        getBinding().showDefinitionView.setVisibility(INVISIBLE);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("ClickableViewAccessibility")
    protected void initTermView() {
        getBinding().termView.setMovementMethod(new ScrollingMovementMethod());
        getDeckDb().deckConfigAsyncDao().getFloatByKey(DeckConfig.STUDY_CARD_TERM_FONT_SIZE)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(deckConfig -> getBinding().termView.setTextSize(deckConfig))
                .subscribe(deckConfig -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        this.getClass().getSimpleName() + "_InitTermView",
                        "Error while read the deck view settings."
                ));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void initDefinitionView() {
        getBinding().definitionView.setMovementMethod(new ScrollingMovementMethod());
        getDeckDb().deckConfigAsyncDao().getFloatByKey(DeckConfig.STUDY_CARD_DEFINITION_FONT_SIZE)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(deckConfig -> getBinding().definitionView.setTextSize(deckConfig))
                .subscribe(deckConfig -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        this.getClass().getSimpleName() + "_InitDefinitionView",
                        "Error while read the deck view settings."
                ));
    }

    protected void onAgainClick(View v) {
        updateCardLearningProgress(model.getAgainLearningProgress().getValue());
        model.nextCard();
    }

    protected void onAgainChanged(CardLearningProgress cardLearningProgress) {
        getBinding().againButton.setText(
                "Again\n" + cardLearningProgress.getInterval() / DIVIDE_MINUTES_TO_DAYS
        );
    }

    protected void onQuickClick(View v) {
        updateCardLearningProgress(model.getQuickLearningProgress().getValue());
        model.nextCard();
    }

    protected void onQuickChanged(CardLearningProgress cardLearningProgress) {
        if (cardLearningProgress != null) {
            getBinding().easyButton.setText(
                    "Quick Repetition\n" + cardLearningProgress.getInterval() + " min"
            );
        } else {
            getBinding().easyButton.setVisibility(INVISIBLE);
        }
    }

    protected void onEasyClick(View v) {
        updateCardLearningProgress(model.getEasyLearningProgress().getValue());
        model.nextCard();
    }

    protected void onEasyChanged(CardLearningProgress cardLearningProgress) {
        getBinding().easyButton.setText(
                "Easy\n" + cardLearningProgress.getInterval() / DIVIDE_MINUTES_TO_DAYS
        );
    }

    protected void onHardChanged(CardLearningProgress cardLearningProgress) {
        getBinding().hardButton.setText(
                "Hard\n" + cardLearningProgress.getInterval() / DIVIDE_MINUTES_TO_DAYS
        );
    }

    protected void onHardClick(View v) {
        updateCardLearningProgress(model.getHardLearningProgress().getValue());
        model.nextCard();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void updateCardLearningProgress(@NonNull CardLearningProgress learningProgress) {
        model.updateLearningProgress(learningProgress)
                .subscribe(() -> refreshLastUpdatedAt()
                        , e -> getExceptionHandler().handleException(
                                e, getSupportFragmentManager(),
                                this.getClass().getSimpleName() + "_UpdateCardLearningProgress",
                                "Error while updating card learning progress."
                        ));
    }

    protected void refreshLastUpdatedAt() {
        getAppDb().deckDaoAsync().refreshLastUpdatedAt(deckDbPath)
                .subscribe(deck -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        this.getClass().getSimpleName() + "_UpdateCardLearningProgress",
                        "Error while updating card learning progress."
                ));
    }

    /* -----------------------------------------------------------------------------------------
     * Activity methods overridden
     * ----------------------------------------------------------------------------------------- */

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
    protected void deleteCard() {
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
                        this.getClass().getSimpleName() + "_OnClickDeleteCard",
                        "Error while removing the card."
                ));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void revertCard(@NonNull Card card) {
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
                        this.getClass().getSimpleName() + "_OnClickRevertCard",
                        "Error while restoring the card."
                ));
    }

    protected void resetView() {
        getBinding().termView.setTextSize(DEFAULT_TERM_FONT_SIZE);
        getBinding().definitionView.setTextSize(DEFAULT_DEFINITION_FONT_SIZE);
    }

    @Override
    protected void onStop() {
        getDeckDb().deckConfigAsyncDao().updateDeckConfig(
                DeckConfig.STUDY_CARD_TERM_FONT_SIZE,
                Float.toString(getBinding().termView.getScaledTextSize()),
                getOnErrorSavingViewSettings()
        );
        getDeckDb().deckConfigAsyncDao().updateDeckConfig(
                DeckConfig.STUDY_CARD_DEFINITION_FONT_SIZE,
                Float.toString(getBinding().definitionView.getScaledTextSize()),
                getOnErrorSavingViewSettings()
        );
        super.onStop();
    }

    @NonNull
    protected Consumer<? super Throwable> getOnErrorSavingViewSettings() {
        return e -> getExceptionHandler().handleException(
                e, getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_OnStop",
                "Error while saving deck view settings."
        );
    }

    @NonNull
    protected String getDeckName(@NonNull String deckDbPath) {
        return deckDbPath.substring(deckDbPath.lastIndexOf("/") + 1);
    }

    protected ActivityStudyCardBinding getBinding() {
        return binding;
    }

    protected DeckDatabase getDeckDb() {
        return deckDb;
    }

    protected AppDatabase getAppDb() {
        return appDb;
    }
}