package pl.softfly.flashcards.ui.deck;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabase;
import pl.softfly.flashcards.ui.card.CardActivity;
import pl.softfly.flashcards.ui.card.ListCardsActivity;
import pl.softfly.flashcards.ui.card.NewCardActivity;

public class DeckListRecyclerViewAdapter extends RecyclerView.Adapter<DeckListRecyclerViewAdapter.ViewHolder> {

    public static final String DECK_NAME = "deckName";

    private final AppCompatActivity activity;

    private final DeckListOnClickListener deckListOnClickListener;

    private final ArrayList<String> deckNames;

    public DeckListRecyclerViewAdapter(AppCompatActivity activity, DeckListOnClickListener deckListOnClickListener, ArrayList<String> deckNames) {
        this.activity = activity;
        this.deckListOnClickListener = deckListOnClickListener;
        this.deckNames = deckNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_list_item, parent, false);
        return new ViewHolder(view, deckListOnClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String deckName = deckNames.get(position);
        holder.nameTextView.setText(deckName);
        holder.nameTextView.setSelected(true);

        DeckDatabase room = AppDatabaseUtil.getInstance().getDeckDatabase(activity.getBaseContext(), deckName);
        room.cardDao().count().observe(activity, count -> {
            holder.totalTextView.setText("Total: " + count);
        });
    }

    @Override
    public int getItemCount() {
        return deckNames.size();
    }

    public interface DeckListOnClickListener {
        void onDeckItemClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTextView;
        TextView moreTextView;
        TextView totalTextView;
        RelativeLayout deckLayoutListItem;
        DeckListOnClickListener deckListOnClickListener;

        public ViewHolder(View itemView, DeckListOnClickListener deckListOnClickListener) {
            super(itemView);
            this.deckListOnClickListener = deckListOnClickListener;
            nameTextView = itemView.findViewById(R.id.nameTextView);
            deckLayoutListItem = itemView.findViewById(R.id.deckListItem);
            itemView.setOnClickListener(this);
            initMoreTextView();
        }

        protected void initMoreTextView() {
            totalTextView = itemView.findViewById(R.id.totalTextView);
            moreTextView = itemView.findViewById(R.id.moreTextView);
            moreTextView.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), moreTextView);
                popup.getMenuInflater().inflate(R.menu.popup_menu_deck, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.listCards: {
                            Intent intent = new Intent(activity, ListCardsActivity.class);
                            intent.putExtra(DECK_NAME, deckNames.get(getAdapterPosition()));
                            activity.startActivity(intent);
                            return true;
                        }
                        case R.id.addCard:
                            Intent intent = new Intent(activity, NewCardActivity.class);
                            intent.putExtra(DECK_NAME, deckNames.get(getAdapterPosition()));
                            activity.startActivity(intent);
                            return true;
                        case R.id.removeDeck:
                            RemoveDeckDialog dialog = new RemoveDeckDialog(deckNames.get(getAdapterPosition()));
                            dialog.show(activity.getSupportFragmentManager(), "RemoveDeck");
                            return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(activity, CardActivity.class);
            intent.putExtra(DECK_NAME, deckNames.get(getAdapterPosition()));
            activity.startActivity(intent);
        }
    }
}
