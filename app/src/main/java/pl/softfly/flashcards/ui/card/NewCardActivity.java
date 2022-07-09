package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CardUtil;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.ActivityNewCardBinding;
import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.ui.base.BaseActivity;

public class NewCardActivity extends BaseActivity {

    public static final String DECK_DB_PATH = "deckDbPath";

    private String deckDbPath;

    @Nullable
    private DeckDatabase deckDb;
    private AppDatabase appDb;

    private CardUtil cardUtil = CardUtil.getInstance();
    private ActivityNewCardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createBinding();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        deckDbPath = intent.getStringExtra(DECK_DB_PATH);
        deckDb = getDeckDatabase(deckDbPath);
        appDb = getAppDatabase();
    }

    protected void createBinding() {
        binding = ActivityNewCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        cardUtil.setTerm(card, getTermEditText().getText().toString());
        cardUtil.setDefinition(card, getDefinitionEditText().getText().toString());
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
                .subscribe(() -> refreshLastUpdatedAt(),
                        e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        this.getClass().getSimpleName() + "_OnClickSaveCard",
                        (dialog, which) -> onBackPressed()
                ));
    }

    protected void refreshLastUpdatedAt() {
        appDb.deckDaoAsync().refreshLastUpdatedAt(deckDbPath)
                .subscribe(deck -> {
                }, e -> getExceptionHandler().handleException(
                        e, getSupportFragmentManager(),
                        this.getClass().getSimpleName(),
                        "Error while creating card."
                ));
    }

    private ActivityNewCardBinding getBinding() {
        return binding;
    }

    protected EditText getTermEditText() {
        return getBinding().termEditText;
    }

    protected EditText getDefinitionEditText() {
        return getBinding().definitionEditText;
    }

    protected CardUtil getCardUtil() {
        return cardUtil;
    }

    @Nullable
    protected DeckDatabase getDeckDb() {
        return deckDb;
    }

    protected AppDatabase getAppDb() {
        return appDb;
    }

    protected String getDeckDbPath() {
        return deckDbPath;
    }
}