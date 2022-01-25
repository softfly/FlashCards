package pl.softfly.flashcards.filesync.task;

import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.concurrent.Callable;

import pl.softfly.flashcards.filesync.algorithms.ExportExcelToDeck;
import pl.softfly.flashcards.filesync.entity.FileSynced;
import pl.softfly.flashcards.tasks.Task;
import pl.softfly.flashcards.ui.cards.file_sync.FileSyncListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ExportExcelFromDeckTask extends SyncExcelToDeckTask implements Callable<Object>, Task<Object> {

    public ExportExcelFromDeckTask(
            String deckName,
            FileSynced fileSynced,
            Uri uriSynchronizedFile,
            @NonNull FileSyncListCardsActivity listCardsActivity
    ) {
        super(deckName, fileSynced, uriSynchronizedFile, listCardsActivity);
    }

    @NonNull
    @Override
    public Object call() throws Exception {
        askPermissions(this.uriSynchronizedFile);
        InputStream isImportedFile = openExcelFile(uriSynchronizedFile);

        ExportExcelToDeck syncExcelToDeck = new ExportExcelToDeck(listCardsActivity);
        syncExcelToDeck.syncExcelFile(deckName, fileSynced, isImportedFile, mimeType);
        syncExcelToDeck.commitChanges(
                fileSynced,
                listCardsActivity
                        .getContentResolver()
                        .openOutputStream(uriSynchronizedFile)
        );

        this.listCardsActivity.runOnUiThread(() -> Toast.makeText(appContext,
                String.format("The deck \"%s\" has been exported to the file.", deckName),
                Toast.LENGTH_LONG)
                .show());
        return true;
    }
}
