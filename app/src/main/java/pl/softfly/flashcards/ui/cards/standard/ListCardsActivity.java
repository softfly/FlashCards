package pl.softfly.flashcards.ui.cards.standard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.IconWithTextInTopbarActivity;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class ListCardsActivity extends IconWithTextInTopbarActivity {

    private String deckName;

    private CardRecyclerViewAdapter adapter;

    public TextView idHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_list_cards);

            idHeader = findViewById(R.id.idHeader);

            Intent intent = getIntent();
            deckName = intent.getStringExtra(DeckRecyclerViewAdapter.DECK_NAME);
            initRecyclerView();

            getSupportActionBar().setElevation(0); // Remove shadow under
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(getSupportFragmentManager(), "ListCardsActivity");
        }
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CardRecyclerViewAdapter adapter = onCreateRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        // ID.WIDTH.3. If the id width has not been calculated, calculate id width.
        if (adapter.idTextViewWidth == 0) { // @todo it is always 0
            recyclerView.getViewTreeObserver()
                    .addOnDrawListener(
                            getCalcCardIdWidth().calcIdWidth(recyclerView, idHeader)
                    );
        }
    }

    protected CardRecyclerViewAdapter onCreateRecyclerViewAdapter() {
        this.adapter = new CardRecyclerViewAdapter(this, deckName);
        return this.adapter;
    }

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

    protected void startNewCardActivity() {
        Intent intent = new Intent(this, NewCardActivity.class);
        intent.putExtra(NewCardActivity.DECK_NAME, deckName);
        this.startActivity(intent);
    }

    protected RecyclerView getRecyclerView() {
        return findViewById(R.id.card_list_view);
    }

    protected String getDeckName() {
        return deckName;
    }

    private CalcCardIdWidth getCalcCardIdWidth() {
        return CalcCardIdWidth.getInstance();
    }

    protected void setAdapter(CardRecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }
}