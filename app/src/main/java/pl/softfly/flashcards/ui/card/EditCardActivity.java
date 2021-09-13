package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

public class EditCardActivity extends NewCardActivity {

    public static final String CARD_ID = "cardId";

    private Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int cardId = intent.getIntExtra(CARD_ID, 0);
        deckDb.cardDao().getCard(cardId).observe(this, card -> {
            this.card = card;
            questionEditText.setText(card.getQuestion());
            answerEditText.setText(card.getAnswer());
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveCard:
                if (Objects.nonNull(card)) {
                    onClickUpdateCard();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onClickUpdateCard() {
        card.setQuestion(questionEditText.getText().toString());
        card.setAnswer(answerEditText.getText().toString());
        deckDb.cardDao().updateAll(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(() -> {
                    Toast.makeText(this, "The card has been updated.", Toast.LENGTH_SHORT).show();
                    super.finish();
                }))
                .subscribe();
    }
}