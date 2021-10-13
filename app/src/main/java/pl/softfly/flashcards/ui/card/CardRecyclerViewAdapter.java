package pl.softfly.flashcards.ui.card;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;

public class CardRecyclerViewAdapter extends RecyclerView.Adapter<CardRecyclerViewAdapter.ViewHolder> {

    private final AppCompatActivity activity;

    private final String deckName;

    private final ArrayList<Card> cards = new ArrayList<>();

    private DeckDatabase deckDb;

    public CardRecyclerViewAdapter(AppCompatActivity activity, String deckName) throws Exception {
        this.activity = activity;
        this.deckName = deckName;
        deckDb = AppDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getDeckDatabase(deckName);
        loadCards();
    }

    public void loadCards() {
        deckDb.cardDaoAsync().getCards()
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(cards -> activity.runOnUiThread(() -> {
                    this.cards.clear();
                    this.cards.addAll(cards);
                    this.notifyDataSetChanged();
                }))
                .subscribe();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.questionTextView.setText(card.getQuestion());
        holder.answerTextView.setText(card.getAnswer());
    }

    @Override
    public int getItemCount() {
        return Objects.nonNull(cards) ? cards.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView questionTextView;
        TextView answerTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            questionTextView = itemView.findViewById(R.id.question);
            answerTextView = itemView.findViewById(R.id.answer);
        }

        @Override
        public void onClick(View view) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.popup_menu_card, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.edit_card: {
                        Intent intent = new Intent(activity, EditCardActivity.class);
                        intent.putExtra(EditCardActivity.DECK_NAME, deckName);
                        intent.putExtra(EditCardActivity.CARD_ID, cards.get(getAdapterPosition()).getId());
                        activity.startActivity(intent);
                        return true;
                    }
                    case R.id.remove_card: {
                        int pos = getAdapterPosition();
                        deckDb.cardDaoAsync().delete(cards.get(pos))
                                .subscribeOn(Schedulers.io())
                                .doOnComplete(() -> activity.runOnUiThread(() -> {
                                    cards.remove(pos);
                                    notifyItemRemoved(pos);
                                    Toast.makeText(activity, "The card has been removed.", Toast.LENGTH_SHORT).show();
                                }))
                                .subscribe();
                        return true;
                    }
                    case R.id.add_card: {
                        Intent intent = new Intent(activity, NewCardActivity.class);
                        intent.putExtra(EditCardActivity.DECK_NAME, deckName);
                        activity.startActivity(intent);
                        return true;
                    }
                }
                return false;
            });
            popup.show();
        }
    }
}
