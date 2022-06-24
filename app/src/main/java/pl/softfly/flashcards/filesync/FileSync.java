package pl.softfly.flashcards.filesync;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import pl.softfly.flashcards.ui.cards.file_sync.FileSyncListCardsActivity;
import pl.softfly.flashcards.ui.deck.standard.ListDecksFragment;

public interface FileSync {

    String TAG = "FileSync";
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

    /**
     * SE Synchronize deck with excel file.
     */
    void syncFile(
            String deckName,
            Uri uri,
            AppCompatActivity activity
    );

    /**
     * IE Create a deck from an imported Excel file.
     */
    void importFile(String importToFolderPath, Uri uri, AppCompatActivity activity);

    /**
     * EE Create a new Excel file from the exported deck.
     */
    void exportFile(
            String deckDbPath,
            Uri uri,
            AppCompatActivity activity
    );
}
