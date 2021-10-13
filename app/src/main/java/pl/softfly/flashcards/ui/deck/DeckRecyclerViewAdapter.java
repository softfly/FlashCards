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

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.card.DraggableViewCardActivity;
import pl.softfly.flashcards.ui.card.ListCardsActivity;
import pl.softfly.flashcards.ui.card.NewCardActivity;

public class DeckRecyclerViewAdapter extends RecyclerView.Adapter<DeckRecyclerViewAdapter.ViewHolder> {

    public static final String DECK_NAME = "deckName";

    private final AppCompatActivity activity;

    private final ArrayList<String> deckNames;

    public DeckRecyclerViewAdapter(AppCompatActivity activity, ArrayList<String> deckNames) {
        this.activity = activity;
        this.deckNames = deckNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String deckName = deckNames.get(position);
        holder.nameTextView.setText(deckName);
        holder.nameTextView.setSelected(true);

        try {
            DeckDatabase deckDb = AppDatabaseUtil
                    .getInstance(activity.getApplicationContext())
                    .getDeckDatabase(deckName);
            deckDb.cardDaoAsync().count().subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .doOnSuccess(count -> activity.runOnUiThread(() -> {
                        holder.totalTextView.setText("Total: " + count);
                    }))
                    .subscribe();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(activity.getSupportFragmentManager(), "DeckRecyclerViewAdapter");
        }
    }

    @Override
    public int getItemCount() {
        return deckNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTextView;
        TextView moreTextView;
        TextView totalTextView;
        RelativeLayout deckLayoutListItem;

        public ViewHolder(View itemView) {
            super(itemView);
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
            Intent intent = new Intent(activity, DraggableViewCardActivity.class);
            intent.putExtra(DECK_NAME, deckNames.get(getAdapterPosition()));
            activity.startActivity(intent);
        }
    }
}
