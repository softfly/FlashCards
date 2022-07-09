package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.ActivityEditCardBinding;
import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.entity.deck.Card;

public class EditCardActivity extends NewCardActivity {

    public static final String CARD_ID = "cardId";

    private Card card;

    private ActivityEditCardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int cardId = intent.getIntExtra(CARD_ID, 0);
        getDeckDb().cardDaoAsync().getCard(cardId).subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(card -> runOnUiThread(() -> {
                    this.card = card;
                    getTermEditText().setText(card.getTerm());
                    getDefinitionEditText().setText(card.getDefinition());
                }))
                .subscribe();

        getDeckDb().cardLearningProgressAsyncDao().findByCardId(cardId).subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(learningProgress -> runOnUiThread(() -> {
                    getNextReplayAtTextDate().setText(learningProgress.getNextReplayAt().toString());
                }))
                .subscribe();
    }

    @Override
    protected void createBinding() {
        binding = ActivityEditCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        getCardUtil().setTerm(card, getTermEditText().getText().toString());
        getCardUtil().setDefinition(card, getDefinitionEditText().getText().toString());
        card.setModifiedAt(TimeUtil.getNowEpochSec());
        getDeckDb().cardDaoAsync().updateAll(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(() -> {
                    Toast.makeText(this, "The card has been updated.", Toast.LENGTH_SHORT).show();
                    super.finish();
                }))
                .subscribe(() -> refreshLastUpdatedAt());
    }

    @Override
    protected void refreshLastUpdatedAt() {
        getAppDb().deckDaoAsync().refreshLastUpdatedAt(getDeckDbPath())
                .subscribe(deck -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        this.getClass().getSimpleName(),
                        "Error while updating card."
                ));
    }

    private ActivityEditCardBinding getBinding() {
        return binding;
    }

    protected EditText getTermEditText() {
        return getBinding().termEditText;
    }

    protected EditText getDefinitionEditText() {
        return getBinding().definitionEditText;
    }

    protected EditText getNextReplayAtTextDate() {
        return getBinding().nextReplayAtTextDate;
    }
}