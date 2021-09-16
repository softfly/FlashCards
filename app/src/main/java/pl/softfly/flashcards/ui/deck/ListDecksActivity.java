package pl.softfly.flashcards.ui.deck;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabase;
import pl.softfly.flashcards.entity.Card;

public class ListDecksActivity extends AppCompatActivity {

    private final ArrayList<String> deckNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_decks);
        loadDecks();
        initRecyclerView();
        FloatingActionButton fabCreateDeck = findViewById(R.id.fab_create_deck);
        fabCreateDeck.setOnClickListener(v -> {
            DialogFragment dialog = new CreateDeckDialog();
            dialog.show(this.getSupportFragmentManager(), "CreateDeck");
        });
        createSampleDeck();
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.deck_list_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new DeckRecyclerViewAdapter(this, deckNames));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void loadDecks() {
        deckNames.clear();
        deckNames.addAll(DeckDatabase.listDatabases());
    }

    protected void createSampleDeck() {
        String deckName = "Sample Deck";
        try {
            DeckDatabase deckDB = AppDatabaseUtil.getInstance().getDeckDatabase(getBaseContext(), deckName);
            deckDB.close();//TODO refactoring
        } catch (Exception e) {
            e.printStackTrace();
        }
        DeckDatabase.removeDatabase(deckName);

        DeckDatabase deckDB = AppDatabaseUtil.getInstance().getDeckDatabase(getBaseContext(), deckName);
        int NUM_CARDS = 20;
        Card[] cards = new Card[NUM_CARDS];
        for (int i = 0; i < NUM_CARDS; i++) {
            StringBuilder questionBuilder = new StringBuilder("Sample question ").append(i + 1);
            StringBuilder answerBuilder = new StringBuilder("Sample answer ").append(i + 1);
            for (int ii = 0; ii < 10; ii++) {
                questionBuilder.append(" Sample question ").append(i + 1);
                answerBuilder.append(" Sample answer ").append(i + 1);
            }
            Card card = new Card();
            card.setQuestion(questionBuilder.toString());
            card.setAnswer(answerBuilder.toString());
            cards[i] = card;
        }

        deckDB.cardDao().insertAll(cards)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> runOnUiThread(this::loadDecks))
                .subscribe();
    }
}