package pl.softfly.flashcards.ui.cards.file_sync;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.entity.deck.Card;
import pl.softfly.flashcards.ui.cards.select.SelectCardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncCardRecyclerViewAdapter extends SelectCardRecyclerViewAdapter {

    private final Set<Card> recentlySyncedCards = new HashSet<>();

    private final FileSyncListCardsActivity activity;

    public FileSyncCardRecyclerViewAdapter(FileSyncListCardsActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
        this.activity = activity;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileSyncCardViewHolder(onCreateView(parent), this);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (recentlySyncedCards.contains(getItem(position))) {
            holder.itemView.setBackgroundColor(
                    MaterialColors.getColor(holder.itemView, R.attr.colorItemRecentlySynced)
            );
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
        Card card = getItem(viewHolder.getBindingAdapterPosition());
        if (recentlySyncedCards.contains(card)) {
            viewHolder.itemView.setBackgroundColor(
                    MaterialColors.getColor(viewHolder.itemView, R.attr.colorItemRecentlySynced)
            );
        }
    }

    @Override
    public FileSyncListCardsActivity getActivity() {
        return activity;
    }
}
