package pl.softfly.flashcards.ui.deck;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.card.study.ExceptionStudyCardActivity;
import pl.softfly.flashcards.ui.cards.exception.ExceptionListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class DeckRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String DECK_NAME = "deckName";
    private static final int VIEW_TYPE_DECK = 1;
    @NonNull
    protected final AppCompatActivity activity;

    protected final ArrayList<String> deckNames = new ArrayList<>();

    protected File currentFolder;

    @NonNull
    private ActivityResultLauncher<String> exportDbDeck;

    public DeckRecyclerViewAdapter(@NonNull AppCompatActivity activity, File currentFolder) {
        this.activity = activity;
        this.currentFolder = currentFolder;
    }

    public void loadItems(@NonNull File path) {
        deckNames.clear();
        deckNames.addAll(AppDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getStorageDb()
                .listDatabases(path)
        );
        activity.runOnUiThread(() -> notifyDataSetChanged());
    }

    protected void exportDb(Uri exportToUri, @NonNull String dbPath) {
        try {
            try (OutputStream out = activity
                    .getContentResolver()
                    .openOutputStream(exportToUri)) {

                AppDatabaseUtil
                        .getInstance(activity.getApplicationContext())
                        .closeDeckDatabase(dbPath);

                try (FileInputStream in = new FileInputStream(dbPath)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FileUtils.copy(in, out);
                    } else {
                        copyFile(in, out);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog eDialog = new ExceptionDialog(e);
            eDialog.show(activity.getSupportFragmentManager(), "ExportDbDeck");
        }
    }

    protected void copyFile(@NonNull FileInputStream in, @NonNull OutputStream out) throws IOException {
        FileChannel inChannel = in.getChannel();
        FileChannel outChannel = ((FileOutputStream) out).getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DECK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (VIEW_TYPE_DECK == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
            return new DeckViewHolder(view, this);
        } else {
            throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int itemPosition) {
        DeckViewHolder deckViewHolder = (DeckViewHolder) holder;
        String deckName = deckNames.get(getDeckPosition(itemPosition));
        deckViewHolder.nameTextView.setText(deckName);
        deckViewHolder.nameTextView.setSelected(true);
        try {
            DeckDatabase deckDb = getDeckDatabase(deckNames.get(getDeckPosition(itemPosition)));
            deckDb.cardDaoAsync().countByNotDeleted().subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .doOnSuccess(count -> activity.runOnUiThread(() ->
                            deckViewHolder.totalTextView.setText("Total: " + count)))
                    .subscribe(integer -> {
                    }, throwable -> {
                    });
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(activity.getSupportFragmentManager(), "DeckRecyclerViewAdapter");
        }
    }

    public void onItemClick(int position) {
        newStudyCardActivity(position);
    }

    protected void newStudyCardActivity(int itemPosition) {
        Intent intent = new Intent(activity, ExceptionStudyCardActivity.class);
        intent.putExtra(
                ExceptionStudyCardActivity.DECK_DB_PATH,
                currentFolder.getPath() + "/" + deckNames.get(getDeckPosition(itemPosition))
        );
        activity.startActivity(intent);
    }

    public void newListCardsActivity(int itemPosition) {
        Intent intent = new Intent(activity, ExceptionListCardsActivity.class);
        intent.putExtra(
                ExceptionListCardsActivity.DECK_DB_PATH,
                currentFolder.getPath() + "/" + deckNames.get(getDeckPosition(itemPosition))
        );
        activity.startActivity(intent);
    }

    public void newNewCardActivity(int itemPosition) {
        Intent intent = new Intent(activity, NewCardActivity.class);
        intent.putExtra(
                NewCardActivity.DECK_DB_PATH,
                currentFolder.getPath() + "/" + deckNames.get(getDeckPosition(itemPosition)));
        activity.startActivity(intent);
    }

    public void showDeleteDeckDialog(int itemPosition) {
        RemoveDeckDialog dialog = new RemoveDeckDialog(
                currentFolder.getPath() + "/" + deckNames.get(getDeckPosition(itemPosition))
        );
        dialog.show(activity.getSupportFragmentManager(), "RemoveDeck");
    }

    public void exportDbDeck(int position) {
        String deckName = deckNames.get(position);

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
                        exportDb(exportedDbUri, deckName);
                }
        );

        exportDbDeck.launch(deckName);
    }

    @Override
    public int getItemCount() {
        return deckNames.size();
    }

    protected int getDeckPosition(int itemPosition) {
        return itemPosition;
    }

    @Nullable
    protected DeckDatabase getDeckDatabase(@NonNull String deckName) {
        return AppDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getDeckDatabase(currentFolder, deckName);
    }
}
