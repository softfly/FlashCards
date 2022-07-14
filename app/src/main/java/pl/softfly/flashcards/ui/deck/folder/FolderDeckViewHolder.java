package pl.softfly.flashcards.ui.deck.folder;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.deck.standard.DeckViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class FolderDeckViewHolder extends DeckViewHolder {

    public FolderDeckViewHolder(@NonNull View itemView, FolderDeckViewAdapter adapter) {
        super(itemView, adapter);
    }

    @Override
    protected boolean onMenuMoreClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cut:
                getAdapter().cut(getBindingAdapterPosition());
                return true;
        }
        return super.onMenuMoreClick(item);
    }

    public FolderDeckViewAdapter getAdapter() {
        return (FolderDeckViewAdapter) super.getAdapter();
    }
}