package pl.softfly.flashcards.ui.deck.standard;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.ui.ExportImportDbUtil;
import pl.softfly.flashcards.ui.FileSyncUtil;
import pl.softfly.flashcards.ui.card.study.display_ratio.DisplayRatioStudyCardActivity;
import pl.softfly.flashcards.ui.card.study.slide.SlideStudyCardActivity;
import pl.softfly.flashcards.ui.main.MainActivity;
import pl.softfly.flashcards.ui.base.recyclerview.BaseViewAdapter;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.card.study.exception.ExceptionStudyCardActivity;
import pl.softfly.flashcards.ui.cards.exception.ExceptionListCardsActivity;
import pl.softfly.flashcards.ui.deck.folder.ListFoldersDecksFragment;

/**
 * @author Grzegorz Ziemski
 */
public class DeckBaseViewAdapter extends BaseViewAdapter<RecyclerView.ViewHolder> {

    protected static final int VIEW_TYPE_DECK = 1;

    protected final ArrayList<String> deckNames = new ArrayList<>();

    public DeckBaseViewAdapter(@NonNull MainActivity activity) {
        super(activity);
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
            return onCreateDeckViewHolder(view);
        } else {
            throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    protected RecyclerView.ViewHolder onCreateDeckViewHolder(View view) {
        return new DeckViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int itemPosition) {
        DeckViewHolder deckViewHolder = (DeckViewHolder) holder;
        deckViewHolder.nameTextView.setText(deckNames.get(getDeckPosition(itemPosition)));
        deckViewHolder.nameTextView.setSelected(true);
        DeckDatabase deckDb = getDeckDatabase(getFullDeckPath(itemPosition));
        deckDb.cardDaoAsync().countByNotDeleted().subscribeOn(Schedulers.io())
                .doOnSuccess(count -> getActivity().runOnUiThread(
                                () -> deckViewHolder.totalTextView.setText("Total: " + count),
                                this::onErrorBindView
                        )
                )
                .subscribe(integer -> {}, this::onErrorBindView);
    }

    protected void onErrorBindView(Throwable e) {
        getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while loading item deck."
        );
    }

    /* -----------------------------------------------------------------------------------------
     * New Activities
     * ----------------------------------------------------------------------------------------- */

    protected void newStudyCardActivity(int itemPosition) {
        Intent intent = new Intent(getActivity(), DisplayRatioStudyCardActivity.class);
        intent.putExtra(
                ExceptionStudyCardActivity.DECK_DB_PATH,
                getFullDeckPath(itemPosition)
        );
        getActivity().startActivity(intent);
    }

    public void newListCardsActivity(int itemPosition) {
        Intent intent = new Intent(getActivity(), ExceptionListCardsActivity.class);
        intent.putExtra(
                ExceptionListCardsActivity.DECK_DB_PATH,
                getFullDeckPath(itemPosition)
        );
        getActivity().startActivity(intent);
    }

    public void newNewCardActivity(int itemPosition) {
        Intent intent = new Intent(getActivity(), NewCardActivity.class);
        intent.putExtra(NewCardActivity.DECK_DB_PATH, getFullDeckPath(itemPosition));
        getActivity().startActivity(intent);
    }

    /* -----------------------------------------------------------------------------------------
     * Actions
     * ----------------------------------------------------------------------------------------- */

    public void onItemClick(int position) {
        newStudyCardActivity(position);
    }

    public void showDeleteDeckDialog(int itemPosition) {
        RemoveDeckDialog dialog = new RemoveDeckDialog(getFullDeckPath(itemPosition), this);
        dialog.show(getActivity().getSupportFragmentManager(), "RemoveDeck");
    }

    public void launchExportDb(int itemPosition) {
        getExportImportDbUtil().launchExportDb(getFullDeckPath(itemPosition));
    }

    public void launchExportToFile(int itemPosition) {
        getFileSyncUtil().launchExportToFile(getFullDeckPath(itemPosition));
    }

    public void launchSyncFile(int itemPosition) {
        getFileSyncUtil().launchSyncFile(getFullDeckPath(itemPosition));
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
                .getInstance(getActivity().getApplicationContext())
                .getStorageDb()
                .listDatabases(openFolder)
        );
        getActivity().runOnUiThread(() -> notifyDataSetChanged());
    }

    protected String getFullDeckPath(int itemPosition) {
        return getCurrentFolder().getPath() + "/" + deckNames.get(getDeckPosition(itemPosition)) + ".db";
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    protected File getRootFolder() {
        return new File(
                DeckDatabaseUtil
                        .getInstance(getActivity().getApplicationContext())
                        .getStorageDb()
                        .getDbFolder()
        );
    }

    protected int getDeckPosition(int itemPosition) {
        return itemPosition;
    }

    protected ListFoldersDecksFragment getListAllDecksFragment() {
        return getActivity().getListAllDecksFragment();
    }

    protected FileSyncUtil getFileSyncUtil() {
        return getActivity().getFileSyncUtil();
    }

    protected ExportImportDbUtil getExportImportDbUtil() {
        return getActivity().getListAllDecksFragment().getExportImportDbUtil();
    }

    @Override
    public int getItemCount() {
        return deckNames.size();
    }

    public File getCurrentFolder() {
        return getRootFolder();
    }

    public MainActivity getActivity() {
        return (MainActivity) super.getActivity();
    }

}
