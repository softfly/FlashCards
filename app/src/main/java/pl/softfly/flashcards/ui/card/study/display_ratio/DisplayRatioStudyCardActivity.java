package pl.softfly.flashcards.ui.card.study.display_ratio;

import android.os.Bundle;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.deck.DeckConfig;
import pl.softfly.flashcards.ui.card.study.zoom.ZoomStudyCardActivity;

/**
 * @author Grzegorz Ziemski
 */
public class DisplayRatioStudyCardActivity extends ZoomStudyCardActivity {

    private final static float DEFAULT_TD_DISPLAY_RATIO = 0.5f;
    private float displayRatio = DEFAULT_TD_DISPLAY_RATIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitialDisplayRatio();
    }

    protected void setInitialDisplayRatio() {
        getDeckDb().deckConfigAsyncDao().getFloatByKey(DeckConfig.STUDY_CARD_TD_DISPLAY_RATIO)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(displayRatio -> {
                    setDisplayRatio(displayRatio);
                    DisplayRatioStudyCardFragment fragment = getAdapter().getActiveFragment();
                    if (fragment != null) fragment.setDisplayRatio(displayRatio);
                })
                .subscribe(deckConfig -> {}, this::onErrorReadDeckSettings);
    }

    @Override
    public void onStop() {
        getDeckDb().deckConfigAsyncDao().updateDeckConfig(
                DeckConfig.STUDY_CARD_TD_DISPLAY_RATIO,
                Float.toString(getDisplayRatio()),
                this::getOnErrorSavingViewSettings
        );
        super.onStop();
    }

    protected float getDisplayRatio() {
        return displayRatio;
    }

    protected void setDisplayRatio(Float displayRatio) {
        this.displayRatio = displayRatio;
    }

    protected DisplayRatioStudyCardAdapter getAdapter() {
        return (DisplayRatioStudyCardAdapter) super.getAdapter();
    }
}
