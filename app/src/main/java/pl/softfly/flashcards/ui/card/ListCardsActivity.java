package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

public class ListCardsActivity extends AppCompatActivity {

    private String deckName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_cards);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DeckRecyclerViewAdapter.DECK_NAME);

        initRecyclerView();
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.card_list_view);
        recyclerView.setAdapter(new CardRecyclerViewAdapter(this, deckName));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}