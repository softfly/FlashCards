package pl.softfly.flashcards.db.deck;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import pl.softfly.flashcards.db.AppStorageDbUtil;
import pl.softfly.flashcards.db.ExternalStorageDbUtil;

/**
 * @author Grzegorz Ziemski
 */
public class DeckDatabaseUtil {

    private final static boolean EXTERNAL_STORAGE = true;

    @NonNull
    private final AppStorageDbUtil<DeckDatabase> deckDbUtil;

    public DeckDatabaseUtil(Context context) {
        this.deckDbUtil = EXTERNAL_STORAGE ?
                new ExternalStorageDbUtil<DeckDatabase>(context) {
                    @NonNull
                    @Override
                    protected Class<DeckDatabase> getTClass() {
                        return DeckDatabase.class;
                    }
                } :
                new AppStorageDbUtil<DeckDatabase>(context) {
                    @NonNull
                    @Override
                    protected Class<DeckDatabase> getTClass() {
                        return DeckDatabase.class;
                    }
                };
    }

    public synchronized DeckDatabase getDatabase(@NonNull String deckName) {
        return deckDbUtil.getDatabase(deckName);
    }

    public List<String> listDatabases() {
        return deckDbUtil.listDatabases();
    }

    public boolean exists(@NonNull String deckName) {
        return deckDbUtil.exists(deckName);
    }

    public void removeDatabase(@NonNull String deckName) {
        deckDbUtil.removeDatabase(deckName);
    }
}