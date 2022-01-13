package pl.softfly.flashcards.filesync;

import android.net.Uri;

import androidx.annotation.Nullable;

import pl.softfly.flashcards.ui.cards.ListCardsActivity;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

public interface FileSync {

    String TYPE_XLS = "application/vnd.ms-excel";
    String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * SE Synchronize deck with excel file.
     */
    void syncFile(
            String deckName,
            Uri uri,
            ListCardsActivity listCardsActivity
    );

    /**
     * IE Create a deck from an imported Excel file.
     */
    void importFile(Uri uri, ListDecksActivity listDecksActivity);

    /**
     * EE Create a new Excel file from the exported deck.
     */
    void exportFile(
            String deckName,
            Uri uri,
            ListCardsActivity listCardsActivity
    );

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
}
