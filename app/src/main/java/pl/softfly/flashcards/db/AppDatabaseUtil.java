package pl.softfly.flashcards.db;

import android.content.Context;

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

    public synchronized DeckDatabase getDeckDatabase(String dbName) {
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

    public DeckDatabaseUtil getDeckDatabaseUtil() {
        return deckDatabaseUtil;
    }
}
