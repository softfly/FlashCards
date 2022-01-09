package pl.softfly.flashcards.filesync;

import android.net.Uri;

import androidx.annotation.Nullable;

import pl.softfly.flashcards.ui.cards.ListCardsActivity;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

public interface FileSync {

    String TYPE_XLS = "application/vnd.ms-excel";
    String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Nullable
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
