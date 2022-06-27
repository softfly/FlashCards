package pl.softfly.flashcards.ui.card.study;

import static android.view.View.OnTouchListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.deck.DeckConfig;

/**
 * It allows adjust the height ratio between the term and the definition.
 */
public class DraggableStudyCardActivity extends StudyCardActivity {

    private final static float DEFAULT_TD_DISPLAY_RATIO = 0.5f;
    private float displayRatio = DEFAULT_TD_DISPLAY_RATIO;

    /**
     * Move divider to move term / definition view boundary.
     */
    private final OnTouchListener moveDividerTouchListener = new OnTouchListener() {

        private final int SAFE_DRAG_AREA_PX = 50;

        private boolean modeDrag;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, @NonNull MotionEvent event) {
            if (isTouchDividerView(event)) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_UP:
                        if (modeDrag) {
                            modeDrag = false;
                            return true;
                        }
                    case MotionEvent.ACTION_MOVE:
                        int height = getBinding().card.getHeight();
                        int minY = (int) (0.2 * height);
                        int maxY = height - minY;
                        int[] point = new int[2];
                        getBinding().card.getLocationOnScreen(point);
                        int absoluteTopY = point[1];

                        float newY = (event.getRawY() - absoluteTopY);
                        if (newY > minY && newY < maxY) {
                            setDisplayRatio(newY / height);
                        }
                        modeDrag = true;
                        return true;
                }
            }
            return false;
        }

        private boolean isTouchDividerView(@NonNull MotionEvent event) {
            int[] guideLinePoint = new int[2];
            getBinding().guideline.getLocationOnScreen(guideLinePoint);

            int startX = guideLinePoint[0];
            int endX = guideLinePoint[0] + getBinding().divider.getWidth();
            int startY = guideLinePoint[1] - SAFE_DRAG_AREA_PX;
            int endY = guideLinePoint[1] + SAFE_DRAG_AREA_PX;

            if (startX < event.getRawX() && event.getRawX() < endX) {
                return startY < event.getRawY() && event.getRawY() < endY;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBinding().divider.setOnTouchListener(moveDividerTouchListener);

        getDeckDb().deckConfigAsyncDao().getFloatByKey(DeckConfig.STUDY_CARD_TD_DISPLAY_RATIO)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(displayRatio -> runOnUiThread(() -> setDisplayRatio(displayRatio)))
                .subscribe(deckConfig -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        this.getClass().getSimpleName() + "_InitDefinitionView",
                        "Error while read the deck view settings."
                ));
    }

    private void setDisplayRatio(Float displayRatio) {
        this.displayRatio = displayRatio;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) getBinding().guideline.getLayoutParams();
        params.guidePercent = displayRatio;
        getBinding().guideline.setLayoutParams(params);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initTermView() {
        super.initTermView();
        getBinding().termView.setOnTouchListener((v, event) -> moveDividerTouchListener.onTouch(getBinding().cardLayout, event));
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initDefinitionView() {
        super.initDefinitionView();
        getBinding().definitionView.setOnTouchListener((v, event) -> moveDividerTouchListener.onTouch(getBinding().cardLayout, event));
    }

    @Override
    protected void resetView() {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) getBinding().guideline.getLayoutParams();
        displayRatio = DEFAULT_TD_DISPLAY_RATIO;
        params.guidePercent = displayRatio;
        getBinding().guideline.setLayoutParams(params);
        super.resetView();
    }

    @Override
    protected void onStop() {
        getDeckDb().deckConfigAsyncDao().updateDeckConfig(
                DeckConfig.STUDY_CARD_TD_DISPLAY_RATIO,
                Float.toString(displayRatio),
                getOnErrorSavingViewSettings()
        );
        super.onStop();
    }
}