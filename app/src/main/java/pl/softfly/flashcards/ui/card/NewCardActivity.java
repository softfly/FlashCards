package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDateTime;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.cards.file_sync.FileSyncListCardsActivity;

public class NewCardActivity extends AppCompatActivity {

    public static final String DECK_NAME = "deckName";

    protected String deckName;

    @Nullable
    protected DeckDatabase deckDb;

    protected EditText termEditText;

    protected EditText definitionEditText;

    protected ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);

        termEditText = findViewById(R.id.termEditText);
        definitionEditText = findViewById(R.id.definitionEditText);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DECK_NAME);
        deckDb = getDeckDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.saveCard:
                onClickSaveCard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    protected Card createCard() {
        Card card = new Card();
        card.setTerm(termEditText.getText().toString());
        card.setDefinition(definitionEditText.getText().toString());
        card.setModifiedAt(LocalDateTime.now());
        return card;
    }

    protected void onClickSaveCard() {
        Completable.fromAction(() ->
                deckDb.cardDao().insertAtEnd(createCard()))
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(() -> {
                            Toast.makeText(this,
                                    "The new card has been added.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            super.finish();
                        })
                )
                .subscribe(() -> {}, e -> exceptionHandler.handleException(
                        e, getSupportFragmentManager(),
                        NewCardActivity.class.getSimpleName() + "_OnClickSaveCard",
                        (dialog, which) -> onBackPressed()
                ));
    }

    @Nullable
    protected DeckDatabase getDeckDatabase() {
        return AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getDeckDatabase(deckName);
    }
}