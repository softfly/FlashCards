package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.ExceptionDialog;

public class NewCardActivity extends AppCompatActivity {

    private final static String TAG = "NewCardActivity";

    public static final String DECK_NAME = "deckName";

    public static final String AFTER_CARD_ID = "afterCardId";

    protected String deckName;

    protected Integer afterCardId;

    protected DeckDatabase deckDb;

    protected EditText questionEditText;

    protected EditText answerEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);

        questionEditText = findViewById(R.id.questionEditText);
        answerEditText = findViewById(R.id.answerEditText);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            Intent intent = getIntent();
            deckName = intent.getStringExtra(DECK_NAME);
            afterCardId = intent.getIntExtra(AFTER_CARD_ID, 0);
            deckDb = AppDatabaseUtil
                    .getInstance(getApplicationContext())
                    .getDeckDatabase(deckName);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(this.getSupportFragmentManager(), TAG);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return  true;
            case R.id.saveCard:
                onClickSaveCard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onClickSaveCard() {
        Card card = new Card();
        card.setQuestion(questionEditText.getText().toString());
        card.setAnswer(answerEditText.getText().toString());

        deckDb.cardDaoAsync().insertAll(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(() -> {
                    Toast.makeText(this, "The new card has been added.", Toast.LENGTH_SHORT).show();
                    super.finish();
                }))
                .subscribe();
        super.finish();
    }
}