package pl.softfly.flashcards.ui.cards.file_sync;

import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.cards.select.SelectCardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncCardRecyclerViewAdapter extends SelectCardRecyclerViewAdapter {

    private final Set<Card> recentlySyncedCards = new HashSet<>();

    private FileSyncListCardsActivity activity;

    public FileSyncCardRecyclerViewAdapter(FileSyncListCardsActivity activity, String deckName) {
        super(activity, deckName);
        this.activity = activity;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileSyncCardViewHolder(onCreateView(parent), this);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        if (selectedCards.contains(getItem(position))) {
            super.onBindViewHolder(holder, position);
        } else if (recentlySyncedCards.contains(getItem(position))) {
            holder.itemView.setBackgroundColor(Color.GREEN);
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    public boolean isShowedRecentlySynced() {
        return !recentlySyncedCards.isEmpty();
    }

    public void showRecentlySynced() {
        getDeckDatabase().fileSyncedDao().findLastSyncAt()
                .subscribeOn(Schedulers.io())
                .subscribe(lastSyncAt ->
                                getDeckDatabase().cardDaoAsync().findByModifiedAt(lastSyncAt)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(cards -> {
                                                    recentlySyncedCards.addAll(cards);
                                                    activity.runOnUiThread(() -> notifyDataSetChanged());
                                                    activity.refreshMenuOnAppBar();
                                                },
                                                e -> exceptionHandler.handleException(
                                                        e, activity.getSupportFragmentManager(),
                                                        CardRecyclerViewAdapter.class.getSimpleName() + "_ShowRecentlySynced"
                                                )
                                        ),
                        e -> exceptionHandler.handleException(
                                e, activity.getSupportFragmentManager(),
                                CardRecyclerViewAdapter.class.getSimpleName() + "_ShowRecentlySynced"
                        )
                );
    }

    public void hideRecentlySynced() {
        recentlySyncedCards.clear();
        activity.refreshMenuOnAppBar();
        this.notifyDataSetChanged();
    }

    @Override
    public void onCardUnselect(@NonNull RecyclerView.ViewHolder viewHolder) {
        super.onCardUnselect(viewHolder);
        Card card = getItem(viewHolder.getAdapterPosition());
        if (recentlySyncedCards.contains(card)) {
            viewHolder.itemView.setBackgroundColor(Color.GREEN);
        }
    }

    @Override
    public FileSyncListCardsActivity getActivity() {
        return activity;
    }
}
