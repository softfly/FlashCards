package pl.softfly.flashcards.ui.card;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.deck.DeckListRecyclerViewAdapter;

import static android.view.View.INVISIBLE;
import static android.view.View.OnTouchListener;
import static android.view.View.VISIBLE;

public class CardActivity extends AppCompatActivity {

    private String deckName;

    private DeckDatabase deckDatabase;

    private ConstraintLayout cardLayout;

    private ConstraintLayout cardView;

    private Guideline guideLineView;

    private View dividerView;
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
    private Card card;
    private TextView questionView;
    private TextView answerView;
    private View gradeButtonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DeckListRecyclerViewAdapter.DECK_NAME);
        deckDatabase = AppDatabaseUtil.getInstance().getDeckDatabase(getBaseContext(), deckName);

        cardLayout = findViewById(R.id.card_layout);
        cardView = findViewById(R.id.card);
        guideLineView = findViewById(R.id.guideline);
        dividerView = findViewById(R.id.divider);
        dividerView.setOnTouchListener(moveDividerTouchListener);
        initQuestionView();
        initAnswerView();

        gradeButtonsLayout = findViewById(R.id.gradeButtonsLayout);
        gradeButtonsLayout.setVisibility(INVISIBLE);
        loadCard();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initQuestionView() {
        questionView = findViewById(R.id.questionView);
        questionView.setMovementMethod(new ScrollingMovementMethod());
        questionView.setOnTouchListener((v, event) -> moveDividerTouchListener.onTouch(cardLayout, event));
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initAnswerView() {
        answerView = findViewById(R.id.answerView);
        answerView.setMovementMethod(new ScrollingMovementMethod());
        answerView.setOnTouchListener((v, event) -> moveDividerTouchListener.onTouch(cardLayout, event));
        answerView.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(VISIBLE);
            answerView.setText(card.getAnswer());
        });
    }

    protected void loadCard() {
        deckDatabase.cardDao().getNextCard().observe(this, card -> {
            questionView.setText(card.getQuestion());
            this.card = card;
        });
    }
}