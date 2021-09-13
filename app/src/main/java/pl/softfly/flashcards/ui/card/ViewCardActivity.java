package pl.softfly.flashcards.ui.card;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public abstract class ViewCardActivity extends AppCompatActivity {

    private String deckName;
    private DeckDatabase deckDatabase;
    private Card card;
    private TextView questionView;
    private TextView answerView;
    private Button againButton;
    private Button easyButton;
    private Button hardButton;
    private View gradeButtonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DeckRecyclerViewAdapter.DECK_NAME);

        deckDatabase = AppDatabaseUtil.getInstance().getDeckDatabase(getBaseContext(), deckName);
        gradeButtonsLayout = findViewById(R.id.gradeButtonsLayout);
        gradeButtonsLayout.setVisibility(INVISIBLE);

        initQuestionView();
        initAnswerView();
        initAgainButton();
        initEasyButton();
        initHardButton();
        loadNextCard();
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
            loadNextCard();
        });
    }

    protected void initEasyButton() {
        easyButton = findViewById(R.id.easyButton);
        easyButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            loadNextCard();
        });
    }

    protected void initHardButton() {
        hardButton = findViewById(R.id.hardButton);
        hardButton.setOnClickListener(v -> {
            gradeButtonsLayout.setVisibility(INVISIBLE);
            loadNextCard();
        });
    }

    protected void loadNextCard() {
        deckDatabase.cardDao().getNextCard(Objects.nonNull(card) ? card.getId() : 0).observe(this, card -> {
            questionView.setText(card.getQuestion());
            answerView.setText("?");
            this.card = card;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}