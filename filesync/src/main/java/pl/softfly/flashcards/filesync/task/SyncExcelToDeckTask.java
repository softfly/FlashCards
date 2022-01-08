package pl.softfly.flashcards.filesync.task;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import pl.softfly.flashcards.filesync.algorithms.SyncExcelToDeck;
import pl.softfly.flashcards.filesync.entity.FileSynced;
import pl.softfly.flashcards.tasks.Task;
import pl.softfly.flashcards.ui.cards.ListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class SyncExcelToDeckTask implements Callable<Object>, Task<Object> {

    private final Uri uriSynchronizedFile;
    private final ListCardsActivity listCardsActivity;
    private final Context appContext;
    private final String deckName;
    private String mimeType;
    private Long lastModifiedAtImportedFile;
    private FileSynced fileSynced;

    public SyncExcelToDeckTask(String deckName,
                               FileSynced fileSynced,
                               Uri uriSynchronizedFile,
                               ListCardsActivity listCardsActivity
    ) {
        this.deckName = deckName;
        this.fileSynced = fileSynced;
        this.uriSynchronizedFile = uriSynchronizedFile;
        this.listCardsActivity = listCardsActivity;
        this.appContext = listCardsActivity.getApplicationContext();
    }

    @Override
    public Object call() throws Exception {
        askPermissions(uriSynchronizedFile);
        InputStream isImportedFile = openExcelFile(uriSynchronizedFile);

        SyncExcelToDeck syncExcelToDeck = new SyncExcelToDeck(listCardsActivity);
        syncExcelToDeck.syncExcelFile(deckName, fileSynced, isImportedFile, mimeType, lastModifiedAtImportedFile);
        syncExcelToDeck.commitChanges(
                fileSynced,
                listCardsActivity
                        .getContentResolver()
                        .openOutputStream(uriSynchronizedFile)
        );

        this.listCardsActivity.runOnUiThread(() -> Toast.makeText(appContext,
                String.format("The deck %s has been synced with the file.", deckName),
                Toast.LENGTH_LONG)
                .show());
        return true;
    }

    private void askPermissions(Uri uri) {
        Intent intent = listCardsActivity.getIntent();
        final int takeFlags = intent.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        listCardsActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }

    protected InputStream openExcelFile(Uri uri) throws FileNotFoundException {
        try (Cursor cursor = listCardsActivity.getContentResolver()
                .query(uri, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                mimeType = cursor.getString(
                        cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                ).toLowerCase();
                lastModifiedAtImportedFile = cursor.getLong(
                        cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                );
            }
        }
        return listCardsActivity.getContentResolver().openInputStream(uri);
    }

    @Override
    public void timeout(Exception e) {
        e.printStackTrace();
        this.listCardsActivity.runOnUiThread(() -> Toast.makeText(appContext, e.toString(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void error(Exception e) {
        e.printStackTrace();
        this.listCardsActivity.runOnUiThread(() -> Toast.makeText(appContext, e.toString(), Toast.LENGTH_LONG).show());
    }
}
