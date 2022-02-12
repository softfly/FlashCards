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

import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.Card;
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
    private ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();

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
        Card card = getItem(position);
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
                    getCurrentList().clear();
                    getCurrentList().addAll(cards);
                    idTextViewWidth = calcIdTextViewWidth();
                    if (positionStart < 0)
                        this.notifyDataSetChanged();
                    else
                        this.notifyItemRangeChanged(positionStart, itemCount);
                }))
                .subscribe(cards1 -> { },
                        e -> exceptionHandler.handleException(
                                e, activity.getSupportFragmentManager(),
                                CardRecyclerViewAdapter.class.getSimpleName() + "_LoadCards",
                                (dialog, which) -> activity.onBackPressed()
                        ));
    }

    private int calcIdTextViewWidth() {
        return Integer.toString(
                getCurrentList().stream()
                        .mapToInt(Card::getOrdinal)
                        .max()
                        .orElse(0)
        ).length() * 30 + 70 + 20;
    }

    public void onClickDeleteCard(int position) {
        deckDb.cardDao().deleteAsync(removeItem(position))
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
                        },
                        e -> exceptionHandler.handleException(
                                e, activity.getSupportFragmentManager(),
                                CardRecyclerViewAdapter.class.getSimpleName() + "_OnClickDeleteCard"
                        ));
    }

    protected void startEditCardActivity(int position) {
        Intent intent = new Intent(activity, EditCardActivity.class);
        intent.putExtra(EditCardActivity.DECK_NAME, deckName);
        intent.putExtra(EditCardActivity.CARD_ID, getItem(position).getId());
        activity.startActivity(intent);
    }

    protected void startNewCardActivity(int position) {
        Intent intent = new Intent(activity, NewCardAfterOrdinalActivity.class);
        intent.putExtra(NewCardActivity.DECK_NAME, deckName);
        intent.putExtra(AFTER_ORDINAL, getItem(position).getOrdinal());
        activity.startActivity(intent);
    }

    @Nullable
    protected DeckDatabase getDeckDatabase() {
        return AppDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getDeckDatabase(deckName);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @NonNull
    public List<Card> getCurrentList() {
        return cards;
    }

    public Card getItem(int position) {
        return cards.get(position);
    }

    protected Card removeItem(int position) {
        return cards.remove(position);
    }

    public ListCardsActivity getActivity() {
        return activity;
    }
}
