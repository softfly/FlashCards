package pl.softfly.flashcards.ui.deck;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.card.study.DraggableStudyCardActivity;
import pl.softfly.flashcards.ui.cards.exception.ExceptionListCardsActivity;
import pl.softfly.flashcards.ui.cards.file_sync.FileSyncListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class DeckRecyclerViewAdapter extends RecyclerView.Adapter<DeckViewHolder> {

    public static final String DECK_NAME = "deckName";

    @NonNull
    private final AppCompatActivity activity;

    private final ArrayList<String> deckNames;

    private String deckName;

    @NonNull
    private final ActivityResultLauncher<String> exportDbDeck;

    public DeckRecyclerViewAdapter(@NonNull AppCompatActivity activity, ArrayList<String> deckNames) {
        this.activity = activity;
        this.deckNames = deckNames;
        this.exportDbDeck = activity.registerForActivityResult(
                new ActivityResultContracts.CreateDocument() {
                    @NonNull
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

    protected void exportDbDeck(Uri exportedDbUri, @NonNull String deckName) {
        try {
            OutputStream out = activity
                    .getContentResolver()
                    .openOutputStream(exportedDbUri);

            AppDatabaseUtil
                    .getInstance(activity.getApplicationContext())
                    .closeDeckDatabase(deckName);

            String dbPath = AppDatabaseUtil
                    .getInstance(activity.getApplicationContext())
                    .getStorageDb()
                    .getDbPath(deckName);

            FileInputStream in = new FileInputStream(dbPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(in, out);
            } else {
                FileChannel inChannel = in.getChannel();
                FileChannel outChannel = ((FileOutputStream) out).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog eDialog = new ExceptionDialog(e);
            eDialog.show(activity.getSupportFragmentManager(), "ExportDbDeck");
        }
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
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
                    .subscribe(integer -> {}, throwable -> {});
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(activity.getSupportFragmentManager(), "DeckRecyclerViewAdapter");
        }
    }

    public void newListCardsActivity(int position) {
        Intent intent = new Intent(activity, ExceptionListCardsActivity.class);
        intent.putExtra(DECK_NAME, deckNames.get(position));
        activity.startActivity(intent);
    }

    public void newNewCardActivity(int position) {
        Intent intent = new Intent(activity, NewCardActivity.class);
        intent.putExtra(DECK_NAME, deckNames.get(position));
        activity.startActivity(intent);
    }

    public void showDeleteDeckDialog(int position) {
        RemoveDeckDialog dialog = new RemoveDeckDialog(deckNames.get(position));
        dialog.show(activity.getSupportFragmentManager(), "RemoveDeck");
    }

    public void exportDbDeck(int position) {
        deckName = deckNames.get(position);
        exportDbDeck.launch(deckName);
    }

    public void mewStudyCardActivity(int position) {
        Intent intent = new Intent(activity, DraggableStudyCardActivity.class);
        intent.putExtra(DECK_NAME, deckNames.get(position));
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return deckNames.size();
    }
}
