package pl.softfly.flashcards.ui.cards.file_sync;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.ui.cards.select.SelectCardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncCardRecyclerViewAdapter extends SelectCardRecyclerViewAdapter {

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
    public FileSyncListCardsActivity getActivity() {
        return activity;
    }
}
