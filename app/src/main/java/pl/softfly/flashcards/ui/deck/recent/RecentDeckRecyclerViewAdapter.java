package pl.softfly.flashcards.ui.deck.recent;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.entity.app.Deck;
import pl.softfly.flashcards.ui.MainActivity;
import pl.softfly.flashcards.ui.deck.standard.DeckRecyclerViewAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class RecentDeckRecyclerViewAdapter extends DeckRecyclerViewAdapter {

    protected final ArrayList<Deck> decks = new ArrayList<>();

    public RecentDeckRecyclerViewAdapter(@NonNull MainActivity activity) {
        super(activity);
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
