package pl.softfly.flashcards.ui.deck;

import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLS;
import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLSX;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.db.deck.DeckDatabaseUtil;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.FileSync;
import pl.softfly.flashcards.ui.ExceptionDialog;

public class ListDecksActivity extends AppCompatActivity {

    private ListDecksActivity listDecksActivity;

    private final ArrayList<String> deckNames = new ArrayList<>();

    @Nullable
    private FileSync fileSync = FileSync.getInstance();

    private final ActivityResultLauncher<String[]> importExcel =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> { if (uri != null) fileSync.importFile(uri, listDecksActivity); }
            );

    private DeckRecyclerViewAdapter deckRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.listDecksActivity = this;
        setContentView(R.layout.activity_list_decks);
        initRecyclerView();
        loadDecks();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_decks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_deck:
                DialogFragment dialog = new CreateDeckDialog();
                dialog.show(this.getSupportFragmentManager(), "CreateDeck");
                return true;
            case R.id.import_excel:
                importExcel.launch(new String[] {TYPE_XLS, TYPE_XLSX});
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                questionBuilder.append("\n Sample question ").append(i + 1);
                answerBuilder.append("\n Sample answer ").append(i + 1);
            }
            Card card = new Card();
            card.setOrdinal(i + 1);
            card.setQuestion(questionBuilder.toString());
            card.setAnswer(answerBuilder.toString());
            card.setModifiedAt(LocalDateTime.now());
            cards[i] = card;
        }

        deckDB.cardDaoAsync().insertAll(cards)
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> runOnUiThread(this::loadDecks))
                .subscribe();
    }
}