package pl.softfly.flashcards.ui.cards.exception;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.ui.cards.file_sync.FileSyncCardRecyclerViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionCardRecyclerViewAdapter extends FileSyncCardRecyclerViewAdapter {

    private final ExceptionListCardsActivity activity;

    public ExceptionCardRecyclerViewAdapter(ExceptionListCardsActivity activity, String deckName) {
        super(activity, deckName);
        this.activity = activity;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExceptionCardViewHolder(onCreateView(parent), this);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        exceptionHandler.tryHandleException(
                () -> super.onBindViewHolder(holder, position),
                getActivity().getSupportFragmentManager(),
                ExceptionListCardsActivity.class.getSimpleName() + "_OnRestart"
        );
    }

    @Override
    public ExceptionListCardsActivity getActivity() {
        return activity;
    }
}