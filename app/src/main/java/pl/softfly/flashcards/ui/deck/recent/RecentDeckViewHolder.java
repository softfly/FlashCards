package pl.softfly.flashcards.ui.deck.recent;

import android.os.Build;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.deck.standard.DeckRecyclerViewAdapter;
import pl.softfly.flashcards.ui.deck.standard.DeckViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class RecentDeckViewHolder extends DeckViewHolder {

    public RecentDeckViewHolder(@NonNull View itemView, DeckRecyclerViewAdapter adapter) {
        super(itemView, adapter);
    }

    @Override
    protected void initMoreTextView() {
        TextView moreTextView = itemView.findViewById(R.id.moreTextView);
        moreTextView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), moreTextView);
            popup.getMenuInflater().inflate(R.menu.popup_menu_deck, popup.getMenu());
            popup.getMenu().removeItem(R.id.cut);
            popup.setOnMenuItemClickListener(this::onMenuMoreClick);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popup.setForceShowIcon(true);
            popup.show();
        });
    }
}