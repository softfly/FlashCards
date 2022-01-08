package pl.softfly.flashcards.filesync;

import android.net.Uri;

import pl.softfly.flashcards.ui.cards.ListCardsActivity;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

public interface FileSync {

    public static final String TYPE_XLS = "application/vnd.ms-excel";
    public static final String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    static FileSync getInstance() {
        try {
            return (FileSync) Class.forName("pl.softfly.flashcards.filesync.FileSyncBean")
                    .getConstructor()
                    .newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void syncFile(String deckName,
                  Uri uriSynchronizedFile,
                  ListCardsActivity listCardsActivity
    );

    void importFile(Uri uri, ListDecksActivity listDecksActivity);
}
