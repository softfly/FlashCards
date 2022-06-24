package pl.softfly.flashcards.ui.deck.recent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.entity.app.Deck;
import pl.softfly.flashcards.ui.MainActivity;
import pl.softfly.flashcards.ui.deck.standard.DeckRecyclerViewAdapter;
import pl.softfly.flashcards.ui.deck.standard.ListDecksFragment;

/**
 * @author Grzegorz Ziemski
 */
public class RecentDeckRecyclerViewAdapter extends DeckRecyclerViewAdapter {

    protected final ArrayList<Deck> decks = new ArrayList<>();

    public RecentDeckRecyclerViewAdapter(@NonNull MainActivity activity, ListDecksFragment listDecksFragment) {
        super(activity, listDecksFragment);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (VIEW_TYPE_DECK == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
            return new RecentDeckViewHolder(view, this);
        } else {
            throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    public void loadItems(@NonNull File openFolder) {
        AppDatabase appDb = AppDatabaseUtil.getInstance(activity.getApplicationContext()).getDatabase();
        appDb.deckDaoAsync()
                .findByLastUpdatedAt(15)
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(decks -> {
                    deckNames.clear();
                    this.decks.clear();
                    this.decks.addAll(decks);
                    deckNames.addAll(
                            decks.stream()
                                    .map(deck -> deck.getName())
                                    .collect(Collectors.toList())
                    );
                    activity.runOnUiThread(() -> notifyDataSetChanged());
                })
                .subscribe();
    }
    
    protected String getFullDeckPath(int itemPosition) {
        return decks.get(getDeckPosition(itemPosition)).getPath();
    }
}
