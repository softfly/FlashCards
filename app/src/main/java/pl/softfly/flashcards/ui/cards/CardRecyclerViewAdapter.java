package pl.softfly.flashcards.ui.cards;

import android.content.Intent;
import android.view.Gravity;
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

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.card.EditCardActivity;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.card.NewCardAfterOrdinalActivity;

import static pl.softfly.flashcards.ui.card.NewCardAfterOrdinalActivity.AFTER_ORDINAL;

public class CardRecyclerViewAdapter extends RecyclerView.Adapter<CardRecyclerViewAdapter.ViewHolder> {

    private final AppCompatActivity activity;

    private CardRecyclerViewAdapter cardRecycler;

    private final String deckName;

    private final ArrayList<Card> cards = new ArrayList<>();

    private DeckDatabase deckDb;

    private Integer cutCardPos;

    public CardRecyclerViewAdapter(AppCompatActivity activity, String deckName) {
        this.activity = activity;
        this.deckName = deckName;
        this.deckDb = getDeckDatabase();
        this.cardRecycler = this;
        loadCards();
    }

    public void loadCards() {
        deckDb.cardDaoAsync().getCardsByDeletedNotOrderByOrdinal()
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(cards -> activity.runOnUiThread(() -> {
                    this.cards.clear();
                    this.cards.addAll(cards);
                    this.notifyDataSetChanged();
                }))
                .subscribe();
    }

    public void refreshCards(int positionStart, int itemCount) {
        deckDb.cardDaoAsync().getCardsByDeletedNotOrderByOrdinal()
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(cards -> activity.runOnUiThread(() -> {
                    this.cards.clear();
                    this.cards.addAll(cards);
                    this.notifyItemRangeChanged(positionStart, itemCount);
                }))
                .subscribe();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.idTextView.setText(card.getOrdinal().toString());
        holder.questionTextView.setText(card.getQuestion());
        holder.answerTextView.setText(card.getAnswer());
    }

    @Override
    public int getItemCount() {
        return Objects.nonNull(cards) ? cards.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView idTextView;
        TextView questionTextView;
        TextView answerTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            idTextView = itemView.findViewById(R.id.id);
            questionTextView = itemView.findViewById(R.id.question);
            answerTextView = itemView.findViewById(R.id.answer);
        }

        @Override
        public void onClick(View view) {
            PopupMenu popup = new PopupMenu(
                    view.getContext(),
                    view,
                    Gravity.LEFT,
                    0,
                    R.style.PopupMenuWithLeftOffset
            );
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
                        onClickRemoveCard();
                        return true;
                    }
                    case R.id.cut_card: {
                        cutCardPos = getAdapterPosition();
                        return true;
                    }
                    case R.id.paste_card: {
                        onClickPasteCard();
                        return true;
                    }
                    case R.id.add_card: {
                        Intent intent = new Intent(activity, NewCardAfterOrdinalActivity.class);
                        intent.putExtra(NewCardActivity.DECK_NAME, deckName);
                        intent.putExtra(
                                AFTER_ORDINAL,
                                cards.get(getAdapterPosition()).getOrdinal()
                        );
                        activity.startActivity(intent);
                        return true;
                    }
                }
                return false;
            });
            showPasteAfterClickCut(popup);
            popup.show();
        }

        protected void showPasteAfterClickCut(PopupMenu popup) {
            if (cutCardPos != null) {
                popup.getMenu().findItem(R.id.paste_card).setVisible(true);
            }
        }

        protected void onClickRemoveCard() {
            int pos = getAdapterPosition();
            deckDb.cardDaoAsync().delete(cards.get(pos).getId())
                    .subscribeOn(Schedulers.io())
                    .doOnComplete(() -> activity.runOnUiThread(() -> {
                        cards.remove(pos);
                        notifyItemRemoved(pos);
                        Toast.makeText(
                                activity,
                                "The card has been removed.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }))
                    .subscribe();
        }

        protected void onClickPasteCard() {
            if (cutCardPos != null) {
                Card cutCard = cards.get(cutCardPos);
                int pasteAfterCardPos = getAdapterPosition();
                Card pasteAfterCard = cards.get(pasteAfterCardPos);
                Completable.fromAction(() ->
                        deckDb.cardDao().changeCardOrdinal(cutCard, pasteAfterCard.getOrdinal())
                )
                        .subscribeOn(Schedulers.io())
                        .doOnComplete(() -> {
                            if (pasteAfterCardPos > cutCardPos) {
                                cardRecycler.refreshCards(
                                        cutCardPos,
                                        pasteAfterCardPos - cutCardPos + 1
                                );
                            } else {
                                cardRecycler.refreshCards(
                                        pasteAfterCardPos,
                                        cutCardPos - pasteAfterCardPos + 1
                                );
                            }
                            cutCardPos=null;
                        })
                        .subscribe();
            }
        }
    }

    protected DeckDatabase getDeckDatabase() {
        return AppDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getDeckDatabase(deckName);
    }
}
