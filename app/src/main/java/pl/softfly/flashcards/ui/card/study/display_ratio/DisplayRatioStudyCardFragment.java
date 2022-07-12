package pl.softfly.flashcards.ui.card.study.display_ratio;

import static android.view.View.OnTouchListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import io.reactivex.rxjava3.functions.Consumer;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.ui.card.study.zoom.ZoomStudyCardFragment;

/**
 * It allows adjust the height ratio between the term and the definition.
 */
public class DisplayRatioStudyCardFragment extends ZoomStudyCardFragment {

    private final static float DEFAULT_TD_DISPLAY_RATIO = 0.5f;

    /**
     * Move divider to move term / definition view boundary.
     */
    private final OnTouchListener moveDividerTouchListener = new OnTouchListener() {

        private final int SAFE_DRAG_AREA_PX = 200;

        private boolean modeDrag;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, @NonNull MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_UP:
                        if (modeDrag) {
                            modeDrag = false;
                            return true;
                        }
                    case MotionEvent.ACTION_MOVE:
                        if (isTouchDividerView(event)) {
                            int height = getDisplayRatioLayout().getHeight();
                            int minY = (int) (0.2 * height);
                            int maxY = height - minY;
                            int[] point = new int[2];
                            getDisplayRatioLayout().getLocationOnScreen(point);
                            int absoluteTopY = point[1];

                            float newY = (event.getRawY() - absoluteTopY);
                            if (newY > minY && newY < maxY) {
                                setDisplayRatio(newY / height);
                            }
                            modeDrag = true;
                            return true;
                        } else {
                            return false;
                        }
                }
            return false;
        }

        private boolean isTouchDividerView(@NonNull MotionEvent event) {
            int[] guideLinePoint = new int[2];
            getGuideline().getLocationOnScreen(guideLinePoint);

            int startX = guideLinePoint[0];
            int endX = guideLinePoint[0] + getDivider().getWidth();
            int startY = guideLinePoint[1] - SAFE_DRAG_AREA_PX;
            int endY = guideLinePoint[1] + SAFE_DRAG_AREA_PX;

            if (startX < event.getRawX() && event.getRawX() < endX) {
                return startY < event.getRawY() && event.getRawY() < endY;
            }
            return false;
        }
    };

    public DisplayRatioStudyCardFragment(DisplayRatioStudyCardAdapter adapter, String deckDbPath, int cardId) {
        super(adapter, deckDbPath, cardId);
    }

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getDivider().setOnTouchListener(moveDividerTouchListener);
        setDisplayRatio(getStudyActivity().getDisplayRatio());
        return view;
    }

    @Override
    protected void initTermView() {
        super.initTermView();
        getTermView().setOnTouchListener(this::onTouch);
    }

    @Override
    protected void initDefinitionView() {
        super.initDefinitionView();
        getDefinitionView().setOnTouchListener(this::onTouch);

    }

    @Override
    protected void initShowDefinitionView() {
        super.initShowDefinitionView();
        getShowDefinitionView().setOnTouchListener(this::onTouch);
    }

    protected boolean onTouch(View v, MotionEvent event) {
        return moveDividerTouchListener.onTouch(getStudyCardLayout(), event);
    }

    /* -----------------------------------------------------------------------------------------
     * Actions
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void resetView() {
        setDisplayRatio(DEFAULT_TD_DISPLAY_RATIO);
        super.resetView();
    }

    protected void setDisplayRatio(Float displayRatio) {
        getStudyActivity().setDisplayRatio(displayRatio);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) getGuideline().getLayoutParams();
        params.guidePercent = displayRatio;
        getGuideline().setLayoutParams(params);
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets for view
     * ----------------------------------------------------------------------------------------- */

    protected Guideline getGuideline() {
        return getBinding().guideline;
    }

    protected View getDivider() {
        return getBinding().divider;
    }

    protected View getDisplayRatioLayout() {
        return getBinding().displayRatioLayout;
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    protected DisplayRatioStudyCardAdapter getAdapter() {
        return (DisplayRatioStudyCardAdapter) super.getAdapter();
    }

    public DisplayRatioStudyCardActivity getStudyActivity() {
        return (DisplayRatioStudyCardActivity) super.getActivity();
    }


}