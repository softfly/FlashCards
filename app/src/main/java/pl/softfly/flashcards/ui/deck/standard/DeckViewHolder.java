package pl.softfly.flashcards.ui.deck.standard;

import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.base.recyclerview.BaseViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class DeckViewHolder extends BaseViewHolder implements View.OnClickListener {

    TextView nameTextView;
    TextView totalTextView;
    RelativeLayout deckLayoutListItem;
    protected DeckViewAdapter adapter;

    public DeckViewHolder(@NonNull View itemView, DeckViewAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        nameTextView = itemView.findViewById(R.id.nameTextView);
        deckLayoutListItem = itemView.findViewById(R.id.deckListItem);
        totalTextView = itemView.findViewById(R.id.totalTextView);
        initMoreTextView();
        itemView.setOnClickListener(this);
    }

    protected void initMoreTextView() {
        TextView moreTextView = itemView.findViewById(R.id.moreTextView);
        moreTextView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), moreTextView);
            popup.getMenuInflater().inflate(R.menu.popup_menu_deck, popup.getMenu());
            popup.setOnMenuItemClickListener(this::onMenuMoreClick);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popup.setForceShowIcon(true);
            popup.show();
        });
    }

    protected boolean onMenuMoreClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.listCards:
                adapter.newListCardsActivity(getBindingAdapterPosition());
                return true;
            case R.id.addCard:
                adapter.newNewCardActivity(getBindingAdapterPosition());
                return true;
            case R.id.removeDeck:
                adapter.showDeleteDeckDialog(getBindingAdapterPosition());
                return true;
            case R.id.more:
                DeckBottomMenu deckBottomMenu = new DeckBottomMenu(getAdapter(), getBindingAdapterPosition());
                deckBottomMenu.show(getAdapter().getActivity().getSupportFragmentManager(), "test");
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        adapter.onItemClick(getBindingAdapterPosition());
    }

    public DeckViewAdapter getAdapter() {
        return adapter;
    }
}