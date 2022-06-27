package pl.softfly.flashcards.ui.cards.file_sync;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
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
import pl.softfly.flashcards.ui.cards.select.SelectCardBaseViewAdapter;
import pl.softfly.flashcards.ui.cards.select.SelectCardViewHolder;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncCardBaseViewAdapter extends SelectCardBaseViewAdapter {

    private Set<Integer> recentlyAddedCards;

    private Set<Integer> recentlyUpdatedCards;

    private Set<Integer> recentlyAddedFileCards;

    private Set<Integer> recentlyUpdatedFileCards;

    public FileSyncCardBaseViewAdapter(FileSyncListCardsActivity activity, String deckDbPath) {
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
        View itemView = holder.itemView;
        if (setBackgroundColorIfContainsCard(itemView, recentlyAddedCards, cardId, R.attr.colorItemRecentlyAddedCard)) return;
        if (setBackgroundColorIfContainsCard(itemView, recentlyUpdatedCards, cardId, R.attr.colorItemRecentlyUpdatedCard)) return;
        if (setBackgroundColorIfContainsCard(itemView, recentlyAddedFileCards, cardId, R.attr.colorItemRecentlyAddedFileCard)) return;
        if (setBackgroundColorIfContainsCard(itemView, recentlyUpdatedFileCards, cardId, R.attr.colorItemRecentlyUpdatedFileCard)) return;
    }

    protected boolean setBackgroundColorIfContainsCard(
            View itemView,
            Set<Integer> cards,
            int cardId,
            @AttrRes int colorAttributeResId
    ) {
        if (cards != null && cards.contains(cardId)) {
            itemView.setBackgroundColor(MaterialColors.getColor(itemView, colorAttributeResId));
            return true;
        }
        return false;
    }

    /* -----------------------------------------------------------------------------------------
     * Items actions
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void loadCards(int positionStart, int itemCount) {
        if (isShowedRecentlySynced()) {
            Completable.fromMaybe(loadCardsToList())
                    .andThen(loadRecentlySynced().toObservable())
                    .doOnComplete(() -> refreshDataSet(positionStart, itemCount, this::onErrorLoadCards))
                    .subscribeOn(Schedulers.io())
                    .subscribe(aLong -> {}, this::onErrorLoadCards);
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
        return getDeckDb().fileSyncedDao().findDeckModifiedAt()
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
        return getDeckDb().cardDaoAsync().findIdsByCreatedAt(createdAt)
                .doOnSuccess(cardIds -> recentlyAddedCards = new HashSet<>(cardIds));
    }

    protected Maybe<List<Integer>> getRecentlyUpdatedDeckCards(long modifiedAt) {
        return getDeckDb().cardDaoAsync().findIdsByModifiedAtAndCreatedAtNot(modifiedAt)
                .doOnSuccess(cardIds -> recentlyUpdatedCards = new HashSet<>(cardIds));
    }

    protected Maybe<List<Integer>> getRecentlyAddedFileCards(long fileSyncCreatedAt) {
        return getDeckDb().cardDaoAsync().findIdsByFileSyncCreatedAt(fileSyncCreatedAt)
                .doOnSuccess(cardIds -> recentlyAddedFileCards = new HashSet<>(cardIds));
    }

    protected Maybe<List<Integer>> getRecentlyUpdatedFileCards(long fileSyncModifiedAt) {
        return getDeckDb().cardDaoAsync().findIdsByFileSyncModifiedAtAndFileSyncCreatedAtNot(fileSyncModifiedAt)
                .doOnSuccess(cardIds -> recentlyUpdatedFileCards = new HashSet<>(cardIds));
    }

    protected void errorShowRecentlySynced(Throwable e) {
        getExceptionHandler().handleException(
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
    public void onCardUnselect(SelectCardViewHolder holder) {
        super.onCardUnselect(holder);
        setRecentlySyncedBackground(holder, holder.getBindingAdapterPosition());
    }

    @Override
    public FileSyncListCardsActivity getActivity() {
        return (FileSyncListCardsActivity) super.getActivity();
    }
}
