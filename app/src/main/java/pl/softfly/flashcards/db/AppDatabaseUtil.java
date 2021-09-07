package pl.softfly.flashcards.db;

import android.content.Context;

import java.util.Map;
import java.util.WeakHashMap;

public class AppDatabaseUtil {

    private static final Map<String, DeckDatabase> DECKS = new WeakHashMap<>();
    private static AppDatabaseUtil INSTANCE;

    public static synchronized AppDatabaseUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppDatabaseUtil();
        }
        return INSTANCE;
    }

    public synchronized DeckDatabase getDeckDatabase(Context context, String dbName) {
        DeckDatabase appDatabase = DECKS.get(dbName);
        if (appDatabase == null) {
            appDatabase = DeckDatabase.getDatabase(context, dbName);
            DECKS.put(dbName, appDatabase);
        }
        return appDatabase;
    }
}
