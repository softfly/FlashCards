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
import pl.softfly.flashcards.db.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.deck.DeckListRecyclerViewAdapter;

public class NewCardActivity extends AppCompatActivity {

    private String deckName;

    private EditText questionEditText;

    private EditText answerEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_card);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DeckListRecyclerViewAdapter.DECK_NAME);

        questionEditText = findViewById(R.id.questionEditText);
        answerEditText = findViewById(R.id.answerEditText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

        DeckDatabase room = AppDatabaseUtil.getInstance().getDeckDatabase(getBaseContext(), deckName);
        room.cardDao().insertAll(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "The new card has been added.", Toast.LENGTH_SHORT).show();
                    });
                })
                .subscribe();

        super.finish();
    }
}