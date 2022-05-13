package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.Card;

public class NewCardAfterOrdinalActivity extends NewCardActivity {

    public static final String AFTER_ORDINAL = "afterOrdinal";

    protected int afterOrdinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        afterOrdinal = intent.getIntExtra(AFTER_ORDINAL, 0);
    }

    protected void onClickSaveCard() {
        Card card = createCard();
        Completable.fromAction(() ->
                        deckDb.cardDao().insertAfterOrdinal(card, afterOrdinal + 1))
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(() -> {
                            Toast.makeText(this,
                                    "The new card has been added.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            super.finish();
                        })
                )
                .subscribe();
    }
}