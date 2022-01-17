package pl.softfly.flashcards.ui.deck;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.FileUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.card.study.DraggableStudyCardActivity;
import pl.softfly.flashcards.ui.cards.ListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class DeckRecyclerViewAdapter extends RecyclerView.Adapter<DeckRecyclerViewAdapter.ViewHolder> {

    public static final String DECK_NAME = "deckName";

    private final AppCompatActivity activity;

    private final ArrayList<String> deckNames;

    private String deckName;

    private final ActivityResultLauncher<String> exportDbDeck;

    public DeckRecyclerViewAdapter(AppCompatActivity activity, ArrayList<String> deckNames) {
        this.activity = activity;
        this.deckNames = deckNames;
        this.exportDbDeck = activity.registerForActivityResult(
                new ActivityResultContracts.CreateDocument() {
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull String input) {
                        return super.createIntent(context, input)
                                .setType("application/vnd.sqlite3");
                    }
                },
                exportedDbUri -> {
                    if (exportedDbUri != null)
                        exportDbDeck(exportedDbUri, deckName);
                }
        );
    }

    protected void exportDbDeck(Uri exportedDbUri, String deckName) {
        try {
            OutputStream exportDbOut = activity
                    .getContentResolver()
                    .openOutputStream(exportedDbUri);

            AppDatabaseUtil
                    .getInstance(activity.getApplicationContext())
                    .closeDeckDatabase(deckName);

            String dbPath = AppDatabaseUtil
                    .getInstance(activity.getApplicationContext())
                    .getDeckDatabaseUtil()
                    .getDbPath(deckName);

            FileUtils.copy(new FileInputStream(dbPath), exportDbOut);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog eDialog = new ExceptionDialog(e);
            eDialog.show(activity.getSupportFragmentManager(), "ExportDbDeck");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String deckName = deckNames.get(position);
        holder.nameTextView.setText(deckName);
        holder.nameTextView.setSelected(true);

        try {
            DeckDatabase deckDb = AppDatabaseUtil
                    .getInstance(activity.getApplicationContext())
                    .getDeckDatabase(deckName);
            deckDb.cardDaoAsync().count().subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .doOnSuccess(count -> activity.runOnUiThread(() -> {
                        holder.totalTextView.setText("Total: " + count);
                    }))
                    .subscribe();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(activity.getSupportFragmentManager(), "DeckRecyclerViewAdapter");
        }
    }

    @Override
    public int getItemCount() {
        return deckNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTextView;
        TextView moreTextView;
        TextView totalTextView;
        RelativeLayout deckLayoutListItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            deckLayoutListItem = itemView.findViewById(R.id.deckListItem);
            itemView.setOnClickListener(this);
            initMoreTextView();
        }

        protected void initMoreTextView() {
            totalTextView = itemView.findViewById(R.id.totalTextView);
            moreTextView = itemView.findViewById(R.id.moreTextView);
            moreTextView.setOnClickListener(v -> {

                PopupMenu popup = new PopupMenu(v.getContext(), moreTextView);
                popup.getMenuInflater().inflate(R.menu.popup_menu_deck, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.listCards: {
                            Intent intent = new Intent(activity, ListCardsActivity.class);
                            intent.putExtra(DECK_NAME, deckNames.get(getAdapterPosition()));
                            activity.startActivity(intent);
                            return true;
                        }
                        case R.id.addCard:
                            Intent intent = new Intent(activity, NewCardActivity.class);
                            intent.putExtra(DECK_NAME, deckNames.get(getAdapterPosition()));
                            activity.startActivity(intent);
                            return true;
                        case R.id.removeDeck:
                            RemoveDeckDialog dialog = new RemoveDeckDialog(deckNames.get(getAdapterPosition()));
                            dialog.show(activity.getSupportFragmentManager(), "RemoveDeck");
                            return true;
                        case R.id.exportDbDeck:
                            deckName = deckNames.get(getAdapterPosition());
                            exportDbDeck.launch(deckName);
                            return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(activity, DraggableStudyCardActivity.class);
            intent.putExtra(DECK_NAME, deckNames.get(getAdapterPosition()));
            activity.startActivity(intent);
        }
    }
}
