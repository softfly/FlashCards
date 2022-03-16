package pl.softfly.flashcards.ui.cards.standard;

import static pl.softfly.flashcards.ui.card.NewCardAfterOrdinalActivity.AFTER_ORDINAL;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.HtmlUtil;
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
    public int idTextViewWidth = 0;
    private ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();
    private CalcCardIdWidth calcCardIdWidth = CalcCardIdWidth.getInstance();
    private HtmlUtil htmlUtil = HtmlUtil.getInstance();

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
        // ID.WIDTH.2. Use the id width if calculated. Use case end.
        if (idTextViewWidth!=0) {
            calcCardIdWidth.setIdWidth(holder, idTextViewWidth);
        }

        if (htmlUtil.isHtml(card.getTerm())) {
            holder.getTermTextView().setText(htmlUtil.fromHtml(card.getTerm()).toString());
        } else {
            holder.getTermTextView().setText(card.getTerm());
        }

        if (htmlUtil.isHtml(card.getDefinition())) {
            holder.getDefinitionTextView().setText(htmlUtil.fromHtml(card.getDefinition()).toString());
        } else {
            holder.getDefinitionTextView().setText(card.getDefinition());
        }
    }

    public void loadCards() {
        loadCards(-1, -1);
    }

    public void loadCards(int positionStart, int itemCount) {
        deckDb.cardDaoAsync().getCardsByDeletedNotOrderByOrdinal()
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .subscribe(cards -> activity.runOnUiThread(() -> {
                            getCurrentList().clear();
                            getCurrentList().addAll(cards);
                            // ID.WIDTH.1. Check if the id width has been calculated for the currently used max id length.
                            idTextViewWidth = calcCardIdWidth.getIdWidth(cards);
                            activity.idHeader.setWidth(idTextViewWidth);
                            if (positionStart < 0)
                                this.notifyDataSetChanged();
                            else
                                this.notifyItemRangeChanged(positionStart, itemCount);
                        }),
                        e -> exceptionHandler.handleException(
                                e, activity.getSupportFragmentManager(),
                                CardRecyclerViewAdapter.class.getSimpleName() + "_LoadCards",
                                "Error while loading cards.",
                                (dialog, which) -> activity.onBackPressed()
                        ));
    }

    public void onClickDeleteCard(int position) {
        Card card = removeItem(position);
        deckDb.cardDao().deleteAsync(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> activity.runOnUiThread(() -> {
                    notifyItemRemoved(position);
                    //Refresh ordinal numbers.
                    loadCards(position, getItemCount() - position);
                    Snackbar.make(getActivity().findViewById(R.id.listCards),
                            "The card has been deleted.",
                            Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> revertCard(card))
                            .show();
                }))
                .subscribe(() -> {}, e -> exceptionHandler.handleException(
                                e, activity.getSupportFragmentManager(),
                                CardRecyclerViewAdapter.class.getSimpleName() + "_OnClickDeleteCard",
                                "Error while removing the card."
                        ));
    }

    private void revertCard(@NonNull Card card) {
        deckDb.cardDao().restoreAsync(card)
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> activity.runOnUiThread(
                        () -> {
                            loadCards(card.getOrdinal() - 1, getItemCount() - card.getOrdinal() + 2);
                            Toast.makeText(activity, "The card has been restored.", Toast.LENGTH_SHORT).show();
                        })
                )
                .subscribe(() -> {
                }, e -> exceptionHandler.handleException(
                        e, activity.getSupportFragmentManager(),
                        CardRecyclerViewAdapter.class.getSimpleName() + "_OnClickRevertCard",
                        "Error while restoring the card."
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
