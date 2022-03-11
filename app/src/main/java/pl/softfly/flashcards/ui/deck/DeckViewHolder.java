package pl.softfly.flashcards.ui.deck;

import android.os.Build;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.R;

/**
 * @author Grzegorz Ziemski
 */
public class DeckViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView nameTextView;
    TextView moreTextView;
    TextView totalTextView;
    RelativeLayout deckLayoutListItem;
    DeckRecyclerViewAdapter adapter;

    public DeckViewHolder(@NonNull View itemView, DeckRecyclerViewAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        nameTextView = itemView.findViewById(R.id.nameTextView);
        deckLayoutListItem = itemView.findViewById(R.id.deckListItem);
        totalTextView = itemView.findViewById(R.id.totalTextView);
        initMoreTextView();
        itemView.setOnClickListener(this);
    }

    protected void initMoreTextView() {
        moreTextView = itemView.findViewById(R.id.moreTextView);
        moreTextView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), moreTextView);
            popup.getMenuInflater().inflate(R.menu.popup_menu_deck, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.listCards: {
                        adapter.newListCardsActivity(getBindingAdapterPosition());
                        return true;
                    }
                    case R.id.addCard:
                        adapter.newNewCardActivity(getBindingAdapterPosition());
                        return true;
                    case R.id.removeDeck:
                        adapter.showDeleteDeckDialog(getBindingAdapterPosition());
                        return true;
                    case R.id.exportDbDeck:
                        adapter.exportDbDeck(getBindingAdapterPosition());
                        return true;
                }
                return false;
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popup.setForceShowIcon(true);
            popup.show();
        });

    }

    @Override
    public void onClick(View view) {
        adapter.mewStudyCardActivity(getBindingAdapterPosition());
    }
}