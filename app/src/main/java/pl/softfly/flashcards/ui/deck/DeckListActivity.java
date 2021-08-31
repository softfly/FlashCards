package pl.softfly.flashcards.ui.deck;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import pl.softfly.flashcards.ui.card.CardActivity;
import pl.softfly.flashcards.ui.deck.DeckListRecyclerViewAdapter.DeckListOnClickListener;
import pl.softfly.flashcards.R;

public class DeckListActivity extends AppCompatActivity implements DeckListOnClickListener {

    private final ArrayList<String> deckNames = new ArrayList<>();

    private Context context;

    private AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getBaseContext();
        activity = this;
        setContentView(R.layout.deck_list);
        loadDecks();
        initRecyclerView();
        FloatingActionButton fabCreateDeck = findViewById(R.id.fab_create_deck);
        fabCreateDeck.setOnClickListener(v -> {
            DialogFragment dialog = new CreateDeckDialog();
            dialog.show(activity.getSupportFragmentManager(), "CreateDeck");
        });
    }

    protected void loadDecks() {
        deckNames.clear();
        File currentPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/flashcards/");
        for (File file: currentPath.listFiles()) {
            if (file.getName().endsWith(".db")) {
                deckNames.add(file.getName().substring(0, file.getName().length()-3));
            }
        }
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.deck_list_view);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new DeckListRecyclerViewAdapter(this,this, deckNames));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onDeckItemClick(int position) {
        startActivity(new Intent(this, CardActivity.class));
    }
}