package pl.softfly.flashcards.ui.cards.standard;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.ActivityListCardsBinding;
import pl.softfly.flashcards.ui.base.IconInTopbarActivity;
import pl.softfly.flashcards.ui.card.NewCardActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ListCardsActivity extends IconInTopbarActivity {

    public static final String DECK_DB_PATH = "deckDbPath";
    private String deckDbPath;
    private CardBaseViewAdapter adapter;
    private ActivityListCardsBinding binding;

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityListCardsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        deckDbPath = intent.getStringExtra(DECK_DB_PATH);
        Objects.requireNonNull(deckDbPath);

        // Remove shadow under bar
        getSupportActionBar().setElevation(0);
        onCreateRecyclerView();
    }

    protected void onCreateRecyclerView() {
        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = onCreateRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        // ID.WIDTH.3. If the id width has not been calculated, calculate id width.
        if (adapter.idTextViewWidth == 0) { // @todo it is always 0
            recyclerView.getViewTreeObserver()
                    .addOnDrawListener(
                            getCalcCardIdWidth().calcIdWidth(recyclerView, binding.idHeader)
                    );
        }
    }

    protected CardBaseViewAdapter onCreateRecyclerViewAdapter() {
        return new CardBaseViewAdapter(this, deckDbPath);
    }

    /* -----------------------------------------------------------------------------------------
     * Activity methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onRestart() {
        super.onRestart();
        adapter.loadCards();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        menu.add(0, R.id.new_card, 1,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_outline_add_24),
                        "New card"
                ));
        getMenuInflater().inflate(R.menu.menu_list_cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.new_card) {
            startNewCardActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshMenuOnAppBar() {
        invalidateOptionsMenu();
    }

    /* -----------------------------------------------------------------------------------------
     * Actions
     * ----------------------------------------------------------------------------------------- */

    protected void startNewCardActivity() {
        Intent intent = new Intent(this, NewCardActivity.class);
        intent.putExtra(NewCardActivity.DECK_DB_PATH, deckDbPath);
        this.startActivity(intent);
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    protected CalcCardIdWidth getCalcCardIdWidth() {
        return CalcCardIdWidth.getInstance();
    }

    protected RecyclerView getRecyclerView() {
        return binding.cardListView;
    }

    protected String getDeckDbPath() {
        return deckDbPath;
    }

    protected CardBaseViewAdapter getAdapter() {
        return adapter;
    }

    public ViewGroup getListCardsView() {
        return binding.listCards;
    }

    public TextView getIdHeader() {
        return binding.idHeader;
    }
}