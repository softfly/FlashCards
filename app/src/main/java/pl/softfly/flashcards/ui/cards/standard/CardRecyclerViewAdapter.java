package pl.softfly.flashcards.ui.cards.standard;

import static pl.softfly.flashcards.ui.card.NewCardAfterOrdinalActivity.AFTER_ORDINAL;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.card.EditCardActivity;
import pl.softfly.flashcards.ui.card.NewCardActivity;
import pl.softfly.flashcards.ui.card.NewCardAfterOrdinalActivity;

public class CardRecyclerViewAdapter extends RecyclerView.Adapter<CardViewHolder> {

    private final ListCardsActivity activity;
    private final String deckName;
    private final List<Card> cards = new LinkedList<>();
    @Nullable
    protected DeckDatabase deckDb;
    private int idTextViewWidth = 0;

    public CardRecyclerViewAdapter(ListCardsActivity activity, String deckName) {
        this.activity = activity;
        this.deckName = deckName;
        this.deckDb = getDeckDatabase();
        loadCards();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardViewHolder(onCreateView(parent), this);
    }

    protected View onCreateView(@NonNull ViewGroup parent) {
        return LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.getIdTextView().setText(card.getOrdinal().toString());
        holder.getIdTextView().setLayoutParams(new TableRow.LayoutParams(
                idTextViewWidth,
                TableRow.LayoutParams.MATCH_PARENT
        ));
        holder.getQuestionTextView().setText(card.getQuestion());
        holder.getAnswerTextView().setText(card.getAnswer());
    }

    public void loadCards() {
        loadCards(-1, -1);
    }

    public void loadCards(int positionStart, int itemCount) {
        deckDb.cardDaoAsync().getCardsByDeletedNotOrderByOrdinal()
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(cards -> activity.runOnUiThread(() -> {
                    this.cards.clear();
                    this.cards.addAll(cards);
                    idTextViewWidth = calcIdTextViewWidth();
                    if (positionStart < 0)
                        this.notifyDataSetChanged();
                    else
                        this.notifyItemRangeChanged(positionStart, itemCount);
                }))
                .subscribe();
    }

    private int calcIdTextViewWidth() {
        return Integer.toString(
                cards.stream()
                        .mapToInt(Card::getOrdinal)
                        .max()
                        .orElse(0)
        ).length() * 20 + 70;
    }

    public void onClickDeleteCard(int position) {
        deckDb.cardDao().deleteAsync(cards.remove(position))
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> activity.runOnUiThread(() -> {
                    loadCards();
                    Toast.makeText(
                            activity,
                            "The card has been deleted.",
                            Toast.LENGTH_SHORT
                    ).show();
                }))
                .subscribe(() -> {
                }, e -> ExceptionDialog.showExceptionDialog(
                        e, activity.getSupportFragmentManager(),
                        "DeleteCard"
                ));
    }

    @Override
    public int getItemCount() {
        return Objects.nonNull(cards) ? cards.size() : 0;
    }

    protected void startEditCardActivity(int position) {
        Intent intent = new Intent(activity, EditCardActivity.class);
        intent.putExtra(EditCardActivity.DECK_NAME, deckName);
        intent.putExtra(EditCardActivity.CARD_ID, cards.get(position).getId());
        activity.startActivity(intent);
    }

    protected void startNewCardActivity(int position) {
        Intent intent = new Intent(activity, NewCardAfterOrdinalActivity.class);
        intent.putExtra(NewCardActivity.DECK_NAME, deckName);
        intent.putExtra(AFTER_ORDINAL, cards.get(position).getOrdinal());
        activity.startActivity(intent);
    }

    @Nullable
    protected DeckDatabase getDeckDatabase() {
        return AppDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getDeckDatabase(deckName);
    }

    public ListCardsActivity getActivity() {
        return activity;
    }

    @NonNull
    public List<Card> getCards() {
        return cards;
    }
}
