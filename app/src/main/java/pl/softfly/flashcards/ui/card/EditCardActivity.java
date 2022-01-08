package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Objects;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.entity.Card;

public class EditCardActivity extends NewCardActivity {

    public static final String CARD_ID = "cardId";

    private Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int cardId = intent.getIntExtra(CARD_ID, 0);
        deckDb.cardDaoAsync().getCard(cardId).subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(card -> runOnUiThread(() -> {
                    this.card = card;
                    questionEditText.setText(card.getQuestion());
                    answerEditText.setText(card.getAnswer());
                }))
                .subscribe();
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
        deckDb.cardDaoAsync().updateAll(createCard())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(() -> {
                    Toast.makeText(this, "The card has been updated.", Toast.LENGTH_SHORT).show();
                    super.finish();
                }))
                .subscribe();
    }
}