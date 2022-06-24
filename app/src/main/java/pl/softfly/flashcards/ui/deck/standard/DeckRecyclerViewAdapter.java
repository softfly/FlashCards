package pl.softfly.flashcards.ui.deck.standard;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.MainActivity;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.card.study.ExceptionStudyCardActivity;
import pl.softfly.flashcards.ui.cards.exception.ExceptionListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class DeckRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final int VIEW_TYPE_DECK = 1;

    @NonNull
    protected final MainActivity activity;

    protected final ListDecksFragment listDecksFragment;

    protected final ArrayList<String> deckNames = new ArrayList<>();

    public DeckRecyclerViewAdapter(@NonNull MainActivity activity, ListDecksFragment listDecksFragment) {
        this.activity = activity;
        this.listDecksFragment = listDecksFragment;
    }

    /* -----------------------------------------------------------------------------------------
     * Adapter methods overridden
     * ----------------------------------------------------------------------------------------- */

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
        deckViewHolder.nameTextView.setText(deckNames.get(getDeckPosition(itemPosition)));
        deckViewHolder.nameTextView.setSelected(true);
        try {
            DeckDatabase deckDb = getDeckDatabase(getFullDeckPath(itemPosition));
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

    /* -----------------------------------------------------------------------------------------
     * New Activities
     * ----------------------------------------------------------------------------------------- */

    protected void newStudyCardActivity(int itemPosition) {
        Intent intent = new Intent(activity, ExceptionStudyCardActivity.class);
        intent.putExtra(
                ExceptionStudyCardActivity.DECK_DB_PATH,
                getFullDeckPath(itemPosition)
        );
        activity.startActivity(intent);
    }

    public void newListCardsActivity(int itemPosition) {
        Intent intent = new Intent(activity, ExceptionListCardsActivity.class);
        intent.putExtra(
                ExceptionListCardsActivity.DECK_DB_PATH,
                getFullDeckPath(itemPosition)
        );
        activity.startActivity(intent);
    }

    public void newNewCardActivity(int itemPosition) {
        Intent intent = new Intent(activity, NewCardActivity.class);
        intent.putExtra(NewCardActivity.DECK_DB_PATH, getFullDeckPath(itemPosition));
        activity.startActivity(intent);
    }

    /* -----------------------------------------------------------------------------------------
     * Actions
     * ----------------------------------------------------------------------------------------- */

    public void onItemClick(int position) {
        newStudyCardActivity(position);
    }

    public void showDeleteDeckDialog(int itemPosition) {
        RemoveDeckDialog dialog = new RemoveDeckDialog(getFullDeckPath(itemPosition), this);
        dialog.show(activity.getSupportFragmentManager(), "RemoveDeck");
    }

    public void launchExportDb(int itemPosition) {
        activity.getListAllDecksFragment().exportImportDbUtil.launchExportDb(getFullDeckPath(itemPosition));
    }

    public void launchExportToFile(int itemPosition) {
        activity.getFileSyncUtil().launchExportToFile(getFullDeckPath(itemPosition));
    }

    public void launchSyncFile(int itemPosition) {
        activity.getFileSyncUtil().launchSyncFile(getFullDeckPath(itemPosition));
    }

    /* -----------------------------------------------------------------------------------------
     * Items actions
     * ----------------------------------------------------------------------------------------- */

    public void refreshItems() {
        loadItems(getCurrentFolder());
    }

    public void loadItems(@NonNull File openFolder) {
        deckNames.clear();
        deckNames.addAll(DeckDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getStorageDb()
                .listDatabases(openFolder)
        );
        activity.runOnUiThread(() -> notifyDataSetChanged());
    }

    protected String getFullDeckPath(int itemPosition) {
        return getCurrentFolder().getPath() + "/" + deckNames.get(getDeckPosition(itemPosition)) + ".db";
    }

    /* -----------------------------------------------------------------------------------------
     * Gets
     * ----------------------------------------------------------------------------------------- */

    @Nullable
    protected DeckDatabase getDeckDatabase(@NonNull String dbPath) {
        return DeckDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getDatabase(dbPath);
    }

    protected File getRootFolder() {
        return new File(
                DeckDatabaseUtil
                        .getInstance(activity.getApplicationContext())
                        .getStorageDb()
                        .getDbFolder()
        );
    }

    @Override
    public int getItemCount() {
        return deckNames.size();
    }

    protected int getDeckPosition(int itemPosition) {
        return itemPosition;
    }

    public File getCurrentFolder() {
        return getRootFolder();
    }
}
