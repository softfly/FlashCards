package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.deck.DeckListRecyclerViewAdapter;

public class ListCardsActivity extends AppCompatActivity {

    private String deckName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        deckName = intent.getStringExtra(DeckListRecyclerViewAdapter.DECK_NAME);

        setContentView(R.layout.activity_list_cards);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initRecyclerView();
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.card_list_view);
        recyclerView.setAdapter(new CardListRecyclerViewAdapter(this, deckName));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}