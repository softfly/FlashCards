package pl.softfly.flashcards.ui.cards.file_sync;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.ui.cards.select.SelectCardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncCardRecyclerViewAdapter extends SelectCardRecyclerViewAdapter {

    private Set<Integer> recentlyAddedCards;

    private Set<Integer> recentlyUpdatedCards;

    private Set<Integer> recentlyAddedFileCards;

    private Set<Integer> recentlyUpdatedFileCards;

    public FileSyncCardRecyclerViewAdapter(FileSyncListCardsActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
    }

    /* -----------------------------------------------------------------------------------------
     * Adapter methods overridden
     * ----------------------------------------------------------------------------------------- */

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileSyncCardViewHolder(onCreateView(parent), this);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        setRecentlySyncedBackground(holder, position);
    }

    protected void setRecentlySyncedBackground(RecyclerView.ViewHolder holder, int position) {
        Card card = getItem(position);
        Integer cardId = card.getId();
        if (recentlyAddedCards != null && recentlyAddedCards.contains(cardId)) {
            holder.itemView.setBackgroundColor(
                    MaterialColors.getColor(holder.itemView, R.attr.colorItemRecentlyAddedCard)
            );
        } else if (recentlyUpdatedCards != null && recentlyUpdatedCards.contains(cardId)) {
            holder.itemView.setBackgroundColor(
                    MaterialColors.getColor(holder.itemView, R.attr.colorItemRecentlyUpdatedCard)
            );
        } else if (recentlyAddedFileCards != null && recentlyAddedFileCards.contains(cardId)) {
            holder.itemView.setBackgroundColor(
                    MaterialColors.getColor(holder.itemView, R.attr.colorItemRecentlyAddedFileCard)
            );
        } else if (recentlyUpdatedFileCards != null && recentlyUpdatedFileCards.contains(cardId)) {
            holder.itemView.setBackgroundColor(
                    MaterialColors.getColor(holder.itemView, R.attr.colorItemRecentlyUpdatedFileCard)
            );
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Items actions
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void loadCards(int positionStart, int itemCount) {
        if (isShowedRecentlySynced()) {
            Completable.fromMaybe(loadCardsToList())
                    .andThen(loadRecentlySynced().toObservable())
                    .doOnComplete(() -> refreshDataSet(positionStart, itemCount))
                    .subscribeOn(Schedulers.io())
                    .subscribe(aLong -> {}, this::errorLoadCards);
        } else {
            super.loadCards(positionStart, itemCount);
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Menu actions
     * ----------------------------------------------------------------------------------------- */

    public void showRecentlySynced() {
        Completable.fromMaybe(loadRecentlySynced())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> getActivity().runOnUiThread(
                        () -> notifyDataSetChanged(),
                        this::errorShowRecentlySynced
                ))
                .subscribe(() -> {
                }, this::errorShowRecentlySynced);
    }

    protected Maybe<Long> loadRecentlySynced() {
        return getDeckDatabase().fileSyncedDao().findDeckModifiedAt()
                .doOnSuccess(deckModifiedAt ->
                        Observable.merge(
                                        getRecentlyAddedDeckCards(deckModifiedAt).toObservable(),
                                        getRecentlyUpdatedDeckCards(deckModifiedAt).toObservable(),
                                        getRecentlyAddedFileCards(deckModifiedAt).toObservable(),
                                        getRecentlyUpdatedFileCards(deckModifiedAt).toObservable()
                                )
                                .doOnComplete(getActivity()::refreshMenuOnAppBar)
                                .blockingSubscribe(listMaybe -> {}, this::errorShowRecentlySynced)
                );
    }

    protected Maybe<List<Integer>> getRecentlyAddedDeckCards(long createdAt) {
        return getDeckDatabase().cardDaoAsync().findIdsByCreatedAt(createdAt)
                .doOnSuccess(cardIds -> recentlyAddedCards = new HashSet<>(cardIds));
    }

    protected Maybe<List<Integer>> getRecentlyUpdatedDeckCards(long modifiedAt) {
        return getDeckDatabase().cardDaoAsync().findIdsByModifiedAtAndCreatedAtNot(modifiedAt)
                .doOnSuccess(cardIds -> recentlyUpdatedCards = new HashSet<>(cardIds));
    }

    protected Maybe<List<Integer>> getRecentlyAddedFileCards(long fileSyncCreatedAt) {
        return getDeckDatabase().cardDaoAsync().findIdsByFileSyncCreatedAt(fileSyncCreatedAt)
                .doOnSuccess(cardIds -> recentlyAddedFileCards = new HashSet<>(cardIds));
    }

    protected Maybe<List<Integer>> getRecentlyUpdatedFileCards(long fileSyncModifiedAt) {
        return getDeckDatabase().cardDaoAsync().findIdsByFileSyncModifiedAtAndFileSyncCreatedAtNot(fileSyncModifiedAt)
                .doOnSuccess(cardIds -> recentlyUpdatedFileCards = new HashSet<>(cardIds));
    }

    protected void errorShowRecentlySynced(Throwable e) {
        exceptionHandler.handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_OnClickShowRecentlySynced",
                "Error while showing recently synced."
        );
    }

    public boolean isShowedRecentlySynced() {
        return recentlyAddedCards != null
                || recentlyUpdatedCards != null
                || recentlyAddedFileCards != null
                || recentlyUpdatedFileCards != null;
    }

    public boolean isShowedRecentlySynced(int position) {
        if (isShowedRecentlySynced()) {
            Card card = getItem(position);
            Integer cardId = card.getId();
            return recentlyAddedCards.contains(cardId)
                    || recentlyUpdatedCards.contains(cardId)
                    || recentlyAddedFileCards.contains(cardId)
                    || recentlyUpdatedFileCards.contains(cardId);
        } else {
            return false;
        }
    }

    public void hideRecentlySynced() {
        recentlyAddedCards = null;
        recentlyUpdatedCards = null;
        recentlyAddedFileCards = null;
        recentlyUpdatedFileCards = null;
        getActivity().refreshMenuOnAppBar();
        this.notifyDataSetChanged();
    }

    @Override
    public void onCardUnselect(@NonNull RecyclerView.ViewHolder holder) {
        super.onCardUnselect(holder);
        setRecentlySyncedBackground(holder, holder.getBindingAdapterPosition());
    }

    @Override
    public FileSyncListCardsActivity getActivity() {
        return getActivity();
    }
}
