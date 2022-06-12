package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.TimeUtil;
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
                    termEditText.setText(card.getTerm());
                    definitionEditText.setText(card.getDefinition());
                }))
                .subscribe();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
        cardUtil.setTerm(card, termEditText.getText().toString());
        cardUtil.setDefinition(card, definitionEditText.getText().toString());
        card.setModifiedAt(TimeUtil.getNowEpochSec());
        deckDb.cardDaoAsync().updateAll(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(() -> {
                    Toast.makeText(this, "The card has been updated.", Toast.LENGTH_SHORT).show();
                    super.finish();
                }))
                .subscribe(() -> refreshLastUpdatedAt());
    }

    @Override
    protected void refreshLastUpdatedAt() {
        appDb.deckDaoAsync().refreshLastUpdatedAt(deckDbPath)
                .subscribe(deck -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        EditCardActivity.class.getSimpleName(),
                        "Error while updating card."
                ));
    }
}