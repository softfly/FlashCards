package pl.softfly.flashcards.ui.card;

import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLS;
import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLSX;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.filesync.FileSync;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

public class ListCardsActivity extends AppCompatActivity {

    private String deckName;

    private CardRecyclerViewAdapter cardRecycler;

    private ListCardsActivity activity;

    private FileSync fileSync = FileSync.getInstance();

    private final ActivityResultLauncher<String[]> syncExcel = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> fileSync.syncFile(deckName, uri, activity)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cards);
        this.activity = this;
        initFabMenu();

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DeckRecyclerViewAdapter.DECK_NAME);

        try {
            initRecyclerView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initRecyclerView() throws Exception {
        RecyclerView recyclerView = findViewById(R.id.card_list_view);
        cardRecycler = new CardRecyclerViewAdapter(this, deckName);
        recyclerView.setAdapter(cardRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void initFabMenu() {
        FloatingActionButton fabCreateCard = findViewById(R.id.fab_create_card);
        fabCreateCard.setOnClickListener(v -> startNewCardActivity());
    }

    @Override
    public void onRestart() {
        super.onRestart();
        cardRecycler.loadCards();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_card:
                startNewCardActivity();
                return true;
            case R.id.sync_excel:
                syncExcel.launch(new String[] {TYPE_XLS, TYPE_XLSX});
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void startNewCardActivity() {
        Intent intent = new Intent(this, NewCardActivity.class);
        intent.putExtra(NewCardActivity.DECK_NAME, deckName);
        this.startActivity(intent);
    }
}