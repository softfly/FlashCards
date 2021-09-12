package pl.softfly.flashcards.ui.card;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabase;
import pl.softfly.flashcards.entity.Card;

public class CardListRecyclerViewAdapter extends RecyclerView.Adapter<CardListRecyclerViewAdapter.ViewHolder> {

    private final AppCompatActivity activity;

    private final ArrayList<Card> cards = new ArrayList<>();

    public CardListRecyclerViewAdapter(AppCompatActivity activity, String deckName) {
        this.activity = activity;
        DeckDatabase deckDB = AppDatabaseUtil.getInstance().getDeckDatabase(activity.getBaseContext(), deckName);
        deckDB.cardDao().getCards().observe(activity, cards -> {
            this.cards.addAll(cards);
            this.notifyItemRangeInserted(0, cards.size());
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_item, parent, false);
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView questionTextView;
        TextView answerTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.question);
            answerTextView = itemView.findViewById(R.id.answer);
        }
    }
}
