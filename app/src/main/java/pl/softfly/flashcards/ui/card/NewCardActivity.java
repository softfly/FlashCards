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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CardUtil;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.Card;

public class NewCardActivity extends AppCompatActivity {

    public static final String DECK_DB_PATH = "deckDbPath";

    protected String deckDbPath;

    @Nullable
    protected DeckDatabase deckDb;
    protected AppDatabase appDb;

    protected EditText termEditText;
    protected EditText definitionEditText;

    protected ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();
    protected CardUtil cardUtil = CardUtil.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);

        termEditText = findViewById(R.id.termEditText);
        definitionEditText = findViewById(R.id.definitionEditText);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        deckDbPath = intent.getStringExtra(DECK_DB_PATH);
        deckDb = initDeckDatabase(deckDbPath);
        appDb = AppDatabaseUtil.getInstance(getApplicationContext()).getAppDatabase();
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
        cardUtil.setTerm(card, termEditText.getText().toString());
        cardUtil.setDefinition(card, definitionEditText.getText().toString());
        card.setModifiedAt(TimeUtil.getNowEpochSec());
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
                .subscribe(() -> {
                    refreshLastUpdatedAt();
                }, e -> exceptionHandler.handleException(
                        e, getSupportFragmentManager(),
                        NewCardActivity.class.getSimpleName() + "_OnClickSaveCard",
                        (dialog, which) -> onBackPressed()
                ));
    }

    protected void refreshLastUpdatedAt() {
        appDb.deckDaoAsync().refreshLastUpdatedAt(deckDbPath)
                .subscribe(deck -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        NewCardActivity.class.getSimpleName(),
                        "Error while creating card."
                ));
    }

    @Nullable
    protected DeckDatabase initDeckDatabase(@NonNull String deckDbPath) {
        return AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getDeckDatabase(deckDbPath);
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}