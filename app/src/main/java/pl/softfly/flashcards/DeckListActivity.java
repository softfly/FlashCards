package pl.softfly.flashcards;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pl.softfly.flashcards.DeckListRecyclerViewAdapter.DeckListOnClickListener;

public class DeckListActivity extends AppCompatActivity implements DeckListOnClickListener {

    private final ArrayList<String> deckNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deck_list);
        initDecks();
        initRecyclerView();
    }

    protected void initDecks() {
        for (int i = 1; i < 15; i++) {
            deckNames.add("Deck name " + i);
        }
        deckNames.add("Veeeeeeeery looooooooong deck name 10");
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.deck_list_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new DeckListRecyclerViewAdapter(this, deckNames));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onDeckItemClick(int position) {
        startActivity(new Intent(this, CardActivity.class));
    }
}