package pl.softfly.flashcards.filesync.task;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.concurrent.Callable;

import pl.softfly.flashcards.filesync.algorithms.ImportExcelToDeck;
import pl.softfly.flashcards.tasks.Task;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

/**
 * https://developer.android.com/training/data-storage/shared/documents-files
 *
 * @author Grzegorz Ziemski
 */
public class ImportExcelToDeckTask implements Callable<Object>, Task<Object> {

    private final ListDecksActivity listDecksActivity;

    private final Context appContext;

    private final Uri uriSynchronizedFile;

    private String fileName;

    private String mimeType;

    public ImportExcelToDeckTask(ListDecksActivity listDecksActivity, Uri uriSynchronizedFile) {
        this.listDecksActivity = listDecksActivity;
        this.appContext = listDecksActivity.getApplicationContext();
        this.uriSynchronizedFile = uriSynchronizedFile;
    }

    @Override
    public Object call() throws IOException {
        askPermissions(uriSynchronizedFile);
        InputStream isImportedFile = openExcelFile(uriSynchronizedFile);
        ImportExcelToDeck ImportExcelToDeck = new ImportExcelToDeck(appContext);
        ImportExcelToDeck.importExcelFile(fileName, isImportedFile, mimeType);

        this.listDecksActivity.runOnUiThread(() -> Toast.makeText(
                appContext,
                String.format("The new deck %s has been imported from an Excel file.", fileName),
                Toast.LENGTH_SHORT
                ).show()
        );
        return true;
    }

    protected void askPermissions(Uri uri) {
        Intent intent = listDecksActivity.getIntent();
        final int takeFlags = intent.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        listDecksActivity.getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }

    protected InputStream openExcelFile(Uri uri) throws FileNotFoundException {
        try (Cursor cursor = listDecksActivity.getContentResolver()
                .query(uri, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                mimeType = cursor.getString(
                        cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                ).toLowerCase();
            }
        }
        return listDecksActivity.getContentResolver().openInputStream(uri);
    }

    public void timeout(Exception e) {
        e.printStackTrace();
        listDecksActivity.runOnUiThread(() -> Toast.makeText(appContext,
                MessageFormat.format("Timeout. The {0} file could not be imported.", fileName),
                Toast.LENGTH_SHORT
        ).show());
    }

    public void error(Exception e) {
        e.printStackTrace();
        listDecksActivity.runOnUiThread(() -> Toast.makeText(appContext,
                MessageFormat.format("Error. The {0} file could not be imported.", fileName),
                Toast.LENGTH_SHORT
        ).show());
    }
}
