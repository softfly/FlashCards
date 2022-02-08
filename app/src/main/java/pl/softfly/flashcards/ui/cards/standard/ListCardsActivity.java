package pl.softfly.flashcards.ui.cards.standard;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class ListCardsActivity extends AppCompatActivity {

    private String deckName;

    private CardRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_list_cards);

            Intent intent = getIntent();
            deckName = intent.getStringExtra(DeckRecyclerViewAdapter.DECK_NAME);
            initRecyclerView();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(getSupportFragmentManager(), "ListCardsActivity");
        }
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(onCreateRecyclerViewAdapter());
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
        menu.add(0, R.id.new_card, 2,
                menuIconWithText(
                        getDrawable(R.drawable.ic_outline_add_24),
                        "New card"
                ));
        menu.add(0, R.id.sync_excel, 2,
                menuIconWithText(
                        getDrawable(R.drawable.ic_sharp_sync_24),
                        "Sync with Excel"
                ));
        menu.add(0, R.id.export_excel, 2,
                menuIconWithText(
                        getDrawable(R.drawable.ic_round_file_upload_24),
                        "Export to new Excel"
                ));
        getMenuInflater().inflate(R.menu.menu_list_cards, menu);
        return true;
    }

    @NonNull
    protected CharSequence menuIconWithText(@NonNull Drawable r, String title) {
        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
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

    protected void setAdapter(CardRecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }
}