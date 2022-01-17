package pl.softfly.flashcards.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.db.deck.DeckDatabaseUtil;

/**
 * @author Grzegorz Ziemski
 */
public class AppDatabaseUtil {

    private static AppDatabaseUtil INSTANCE;

    private final Map<String, DeckDatabase> DECKS = new WeakHashMap<>();

    @NonNull
    private final DeckDatabaseUtil deckDatabaseUtil;

    protected AppDatabaseUtil(Context context) {
        this.deckDatabaseUtil = new DeckDatabaseUtil(context);
    }

    public static synchronized AppDatabaseUtil getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppDatabaseUtil(context);
        }
        return INSTANCE;
    }

    @Nullable
    public synchronized DeckDatabase getDeckDatabase(@NonNull String dbName) {
        DeckDatabase db = DECKS.get(dbName);
        if (db == null) {
            db = deckDatabaseUtil.getDatabase(dbName);
            DECKS.put(dbName, db);
        } else if (!db.isOpen()) {
            db = deckDatabaseUtil.getDatabase(dbName);
            DECKS.put(dbName, db);
        }
        return db;
    }

    public synchronized void closeDeckDatabase(String dbName) {
        DeckDatabase db = DECKS.get(dbName);
        if (db != null) {
            if (db.isOpen()) {
                db.close();
            }
            DECKS.remove(dbName);
        }
    }

    @NonNull
    public DeckDatabaseUtil getDeckDatabaseUtil() {
        return deckDatabaseUtil;
    }
}
