package pl.softfly.flashcards.ui.deck;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.db.deck.DeckDatabaseUtil;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.tasks.LongTasksExecutor;
import pl.softfly.flashcards.tasks.Task;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.card.ListCardsActivity;

import static pl.softfly.flashcards.filesync.FileSyncConstants.TYPE_XLS;
import static pl.softfly.flashcards.filesync.FileSyncConstants.TYPE_XLSX;

public class ListDecksActivity extends AppCompatActivity {

    private final ArrayList<String> deckNames = new ArrayList<>();

    private final ActivityResultLauncher<String[]> importExcel = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                try {
                    LongTasksExecutor.getInstance().doTask((Task<Object>)
                            Class.forName("pl.softfly.flashcards.filesync.task.ImportExcelToDeckTask")
                                    .getConstructor(ListCardsActivity.class,  Uri.class)
                                    .newInstance( this, uri));
                } catch (Exception e) {
                    e.printStackTrace();
                    //@todo meesage
                }
            }
    );

    private DeckRecyclerViewAdapter deckRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_decks);
        initRecyclerView();
        loadDecks();
        FloatingActionButton fabCreateDeck = findViewById(R.id.fab_create_deck);
        fabCreateDeck.setOnClickListener(v -> {
            DialogFragment dialog = new CreateDeckDialog();
            dialog.show(this.getSupportFragmentManager(), "CreateDeck");
        });
        FloatingActionButton fabImportDeck = findViewById(R.id.import_deck);
        fabImportDeck.setOnClickListener(
                v -> importExcel.launch(new String[]{TYPE_XLS, TYPE_XLSX}));
        try {
            createSampleDeck();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(this.getSupportFragmentManager(), "CreateDeck");
        }
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.deck_list_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        deckRecyclerViewAdapter = new DeckRecyclerViewAdapter(this, deckNames);
        recyclerView.setAdapter(deckRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void loadDecks() {
        deckNames.clear();
        deckNames.addAll(AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getDeckDatabaseUtil()
                .listDatabases()
        );
        deckRecyclerViewAdapter.notifyDataSetChanged();
    }

    protected void createSampleDeck() throws Exception {
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }

        String deckName = "Sample Deck";
        DeckDatabaseUtil deckDatabaseUtil = AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getDeckDatabaseUtil();
        if (deckDatabaseUtil.exists(deckName)) {
            deckDatabaseUtil.removeDatabase(deckName);
        }

        DeckDatabase deckDB = AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getDeckDatabase(deckName);

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

        deckDB.cardDaoAsync().insertAll(cards)
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> runOnUiThread(this::loadDecks))
                .subscribe();
    }
}