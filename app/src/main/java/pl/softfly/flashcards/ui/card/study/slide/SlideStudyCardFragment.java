package pl.softfly.flashcards.ui.card.study.slide;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.HtmlUtil;
import pl.softfly.flashcards.databinding.ActivityStudyCardBinding;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.entity.deck.CardLearningProgress;
import pl.softfly.flashcards.ui.base.BaseFragment;
import pl.softfly.flashcards.ui.card.study.zoom.ZoomTextView;

/**
 * @author Grzegorz Ziemski
 */
public class SlideStudyCardFragment extends BaseFragment {

    private final static float DIVIDE_MINUTES_TO_HOURS = 60;
    private final static float DIVIDE_MINUTES_TO_DAYS = 24 * 60;

    private final HtmlUtil htmlUtil = HtmlUtil.getInstance();
    private final String deckDbPath;
    private final int cardId;
    private final SlideStudyCardAdapter adapter;
    private ActivityStudyCardBinding binding;
    private DeckDatabase deckDb;
    private AppDatabase appDb;
    private StudyCardViewModel model;
    private Card card;

    public SlideStudyCardFragment(SlideStudyCardAdapter adapter, String deckDbPath, int cardId) {
        Objects.nonNull(adapter);
        Objects.nonNull(deckDbPath);
        Objects.nonNull(cardId);
        this.adapter = adapter;
        this.deckDbPath = deckDbPath;
        this.cardId = cardId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deckDb = getDeckDatabase(deckDbPath);
        appDb = getAppDatabase();

        // TODO constructor
        model = new ViewModelProvider(this).get(StudyCardViewModel.class);
        model.setDeckDb(deckDb);
        model.setAppDb(appDb);


    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = ActivityStudyCardBinding.inflate(getLayoutInflater());

        initTermView();
        initDefinitionView();
        initShowDefinitionView();

        getGradeButtonsLayout().setVisibility(INVISIBLE);
        getAgainButton().setOnClickListener(this::onAgainClick);
        getQuickButton().setVisibility(View.GONE);
        getQuickButton().setOnClickListener(this::onQuickClick);
        getModel().getQuickLearningProgress().observe(getViewLifecycleOwner(), this::onQuickChanged);
        getEasyButton().setOnClickListener(this::onEasyClick);
        getModel().getEasyLearningProgress().observe(getViewLifecycleOwner(), this::onEasyChanged);
        getHardButton().setOnClickListener(this::onHardClick);
        getModel().getHardLearningProgress().observe(getViewLifecycleOwner(), this::onHardChanged);
        loadCard(cardId);
        return getBinding().getRoot();
    }

    protected void loadCard(Integer cardId) {
        getDeckDb().cardDaoAsync().getCard(cardId)
                .doOnSuccess(card -> {
                    this.card = card;
                    setText(getTermView(), card.getTerm());
                    setText(getDefinitionView(), card.getDefinition());
                    hideDefinition();
                    getModel().setCard(cardId, this::onErrorLoadCard);
                })
                .subscribeOn(Schedulers.io())
                .subscribe(card -> {}, this::onErrorUpdateCardLearningProgress);
    }

    protected void onErrorLoadCard(Throwable e) {
        getExceptionHandler().handleException(
                e, getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while read the card."
        );
    }

    protected void setText(TextView textView, String value) {
        if (htmlUtil.isHtml(value)) {
            textView.setText(htmlUtil.fromHtml(value));
        } else {
            textView.setText(value);
        }
    }

    protected void onClickShowDefinition(View view) {
        getGradeButtonsLayout().setVisibility(VISIBLE);
        getDefinitionView().setVisibility(VISIBLE);
        getShowDefinitionView().setVisibility(INVISIBLE);
    }

    @UiThread
    protected void hideDefinition() {
        getGradeButtonsLayout().setVisibility(INVISIBLE);
        getDefinitionView().setVisibility(INVISIBLE);
        getShowDefinitionView().setVisibility(VISIBLE);
    }

    protected void initTermView() {
        getTermView().setMovementMethod(new ScrollingMovementMethod());
    }

    protected void initDefinitionView() {
        getDefinitionView().setMovementMethod(new ScrollingMovementMethod());
    }

    protected void initShowDefinitionView() {
        getShowDefinitionView().setOnClickListener(this::onClickShowDefinition);
    }

    protected void onAgainClick(View v) {
        getModel().updateLearningProgress(
                        getModel().getAgainLearningProgress().getValue(),
                        getDeckDbPath(),
                        this::onErrorUpdateCardLearningProgress
                )
                .doOnComplete(() -> {
                    runOnUiThread(() -> {
                                hideDefinition();
                                getAdapter().showNextCard();
                            },
                            this::onErrorUpdateCardLearningProgress);
                    getModel().setCard(cardId, this::onErrorLoadCard);
                })
                .subscribe(() -> {}, this::onErrorUpdateCardLearningProgress);
    }

    protected void onQuickClick(View v) {
        updateCardLearningProgress(getModel().getQuickLearningProgress().getValue());
    }

    protected void onEasyClick(View v) {
        updateCardLearningProgress(getModel().getEasyLearningProgress().getValue());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void updateCardLearningProgress(@NonNull CardLearningProgress learningProgress) {
        getModel().updateLearningProgress(
                        learningProgress,
                        getDeckDbPath(),
                        this::onErrorUpdateCardLearningProgress
                )
                .doOnComplete(() -> getAdapter().removeCardFromView())
                .subscribe(() -> {}, this::onErrorUpdateCardLearningProgress);
    }

    protected void onErrorUpdateCardLearningProgress(Throwable e) {
        getExceptionHandler().handleException(
                e, getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while update learning progress."
        );
    }

    protected void onHardClick(View v) {
        updateCardLearningProgress(getModel().getHardLearningProgress().getValue());
    }

    protected void onQuickChanged(CardLearningProgress cardLearningProgress) {
        if (cardLearningProgress != null) {
            getQuickButton().setVisibility(VISIBLE);
            getQuickButton().setText("Quick Repetition\n" + displayInterval(cardLearningProgress.getInterval()));
        }
    }

    protected void onEasyChanged(CardLearningProgress cardLearningProgress) {
        if (cardLearningProgress.getId() == null || cardLearningProgress.getRemembered().equals(false)) {
            getEasyButton().setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            getEasyButton().setText("Easy\n" + displayIntervalWithTimeUnit(cardLearningProgress.getInterval()));
        } else {
            getEasyButton().setText("Easy\n" + displayInterval(cardLearningProgress.getInterval()));
        }
    }

    protected void onHardChanged(CardLearningProgress cardLearningProgress) {
        if (cardLearningProgress.getId() == null || cardLearningProgress.getRemembered().equals(false)) {
            getHardButton().setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            getHardButton().setText("Hard\n" + displayIntervalWithTimeUnit(cardLearningProgress.getInterval()));
        } else {
            getHardButton().setText("Hard\n" + displayInterval(cardLearningProgress.getInterval()));
        }
    }

    protected String displayIntervalWithTimeUnit(float interval) {
        if (interval < 24 * 60) {
            return displayInterval(interval);
        } else if (interval < 2 * 24 * 60) {
            return String.format("%.1f", interval / DIVIDE_MINUTES_TO_DAYS) + " day";
        }
        return String.format("%.1f", interval / DIVIDE_MINUTES_TO_DAYS) + " days";
    }

    protected String displayInterval(float interval) {
        if (interval < 60) {
            return (int) interval + " min";
        } else if (interval < 24 * 60) {
            return String.format("%.1f", interval / DIVIDE_MINUTES_TO_HOURS) + " h";
        }
        return String.format("%.1f", interval / DIVIDE_MINUTES_TO_DAYS);
    }

    /* -----------------------------------------------------------------------------------------
     * Fragment methods overridden
     * ----------------------------------------------------------------------------------------- */

    public void onResume() {
        super.onResume();
        getAdapter().setActiveFragment(this);
    }

    /* -----------------------------------------------------------------------------------------
     * Menu actions
     * ----------------------------------------------------------------------------------------- */

    public void deleteCard() {
        Card card = getCard();
        getDeckDb().cardDao().deleteAsync(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    getAdapter().removeCardFromView();
                    Snackbar.make(getBinding().getRoot().getRootView(),
                                    "The card has been deleted.",
                                    Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> revertCard(card))
                            .show();
                })
                .subscribe(() -> {}, this::onErrorDeleteCard);
    }

    protected void onErrorDeleteCard(Throwable e) {
        getExceptionHandler().handleException(
                e, getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while removing the card."
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void revertCard(@NonNull Card card) {
        getDeckDb().cardDao().restoreAsync(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(
                        () -> Toast.makeText(
                                getContext(),
                                "The card has been restored.",
                                Toast.LENGTH_SHORT
                        ).show(), this::onErrorRevertCard)
                )
                .subscribe(() -> {}, this::onErrorRevertCard);
    }

    protected void onErrorRevertCard(Throwable e) {
        getExceptionHandler().handleException(
                e, getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while restoring the card."
        );
    }

    public void resetView() {
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets for view
     * ----------------------------------------------------------------------------------------- */

    protected ActivityStudyCardBinding getBinding() {
        return binding;
    }

    protected ConstraintLayout getStudyCardLayout() {
        return getBinding().studyCardLayout;
    }

    protected ZoomTextView getTermView() {
        return getBinding().termView;
    }

    protected ZoomTextView getDefinitionView() {
        return getBinding().definitionView;
    }

    protected TextView getShowDefinitionView() {
        return getBinding().showDefinitionView;
    }

    protected LinearLayout getGradeButtonsLayout() {
        return getBinding().gradeButtonsLayout;
    }

    protected Button getAgainButton() {
        return getBinding().againButton;
    }

    protected Button getQuickButton() {
        return getBinding().quickButton;
    }

    protected Button getEasyButton() {
        return getBinding().easyButton;
    }

    protected Button getHardButton() {
        return getBinding().hardButton;
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    protected DeckDatabase getDeckDb() {
        return deckDb;
    }

    protected FragmentManager getSupportFragmentManager() {
        return getActivity().getSupportFragmentManager();
    }

    protected int getCardId() {
        return cardId;
    }

    public Card getCard() {
        return card;
    }

    protected SlideStudyCardAdapter getAdapter() {
        return adapter;
    }

    protected String getDeckDbPath() {
        return deckDbPath;
    }

    protected StudyCardViewModel getModel() {
        return model;
    }

}
