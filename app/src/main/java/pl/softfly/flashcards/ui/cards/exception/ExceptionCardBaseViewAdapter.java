package pl.softfly.flashcards.ui.cards.exception;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.ui.cards.file_sync.FileSyncCardBaseViewAdapter;
import pl.softfly.flashcards.ui.cards.standard.CardViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionCardBaseViewAdapter extends FileSyncCardBaseViewAdapter {

    public ExceptionCardBaseViewAdapter(ExceptionListCardsActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExceptionCardViewHolder(onCreateView(parent), this);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        getExceptionHandler().tryRun(
                () -> super.onBindViewHolder(holder, position),
                getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_OnBindViewHolder"
        );
    }
}