package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

public class ListCardsActivity extends AppCompatActivity {

    private String deckName;

    private CardRecyclerViewAdapter cardRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cards);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DeckRecyclerViewAdapter.DECK_NAME);

        initRecyclerView();
        initFabMenu();
    }

    protected void initRecyclerView() {
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