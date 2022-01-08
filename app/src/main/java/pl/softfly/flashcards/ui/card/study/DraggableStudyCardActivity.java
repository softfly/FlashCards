package pl.softfly.flashcards.ui.card.study;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.card.study.StudyCardActivity;

import static android.view.View.OnTouchListener;

public class DraggableStudyCardActivity extends StudyCardActivity {

    private ConstraintLayout cardLayout;
    private ConstraintLayout cardView;
    private Guideline guideLineView;
    private View dividerView;
    private TextView questionView;
    private TextView answerView;

    /**
     * Move divider to move question / answer view boundary.
     */
    private final OnTouchListener moveDividerTouchListener = new OnTouchListener() {

        private final int SAFE_DRAG_AREA_PX = 50;

        private boolean modeDrag;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (isDividerView(event)) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_UP:
                        if (modeDrag) {
                            modeDrag = false;
                            return true;
                        }
                    case MotionEvent.ACTION_MOVE:
                        int height = cardView.getHeight();
                        int minY = (int) (0.2 * height);
                        int maxY = height - minY;
                        int[] point = new int[2];
                        cardView.getLocationOnScreen(point);
                        int absoluteTopY = point[1];

                        float newY = (event.getRawY() - absoluteTopY);
                        if (newY > minY && newY < maxY) {
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideLineView.getLayoutParams();
                            params.guidePercent = newY / height;
                            guideLineView.setLayoutParams(params);
                        }
                        modeDrag = true;
                        return true;
                }
            }
            return false;
        }

        private boolean isDividerView(MotionEvent event) {
            int[] guideLinePoint = new int[2];
            guideLineView.getLocationOnScreen(guideLinePoint);

            int startX = guideLinePoint[0];
            int endX = guideLinePoint[0] + dividerView.getWidth();
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
        cardLayout = findViewById(R.id.cardLayout);
        cardView = findViewById(R.id.card);
        guideLineView = findViewById(R.id.guideline);
        dividerView = findViewById(R.id.divider);
        dividerView.setOnTouchListener(moveDividerTouchListener);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initQuestionView() {
        super.initQuestionView();
        questionView = findViewById(R.id.questionView);
        questionView.setOnTouchListener((v, event) -> moveDividerTouchListener.onTouch(cardLayout, event));
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initAnswerView() {
        super.initAnswerView();
        answerView = findViewById(R.id.answerView);
        answerView.setOnTouchListener((v, event) -> moveDividerTouchListener.onTouch(cardLayout, event));
    }
}