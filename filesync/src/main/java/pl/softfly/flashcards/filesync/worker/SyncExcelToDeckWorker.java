package pl.softfly.flashcards.filesync.worker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.FileNotFoundException;
import java.io.InputStream;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.FlashCardsApp;
import pl.softfly.flashcards.filesync.algorithms.SyncExcelToDeck;
import pl.softfly.flashcards.filesync.db.FileSyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.FileSyncDeckDatabase;
import pl.softfly.flashcards.filesync.entity.FileSynced;

/**
 * This class separates the Android API from the algorithm.
 *
 * @author Grzegorz Ziemski
 */
public class SyncExcelToDeckWorker extends Worker {

    public static final String DECK_NAME = "DECK_NAME";
    public static final String FILE_URI = "FILE_URI";
    public static final String AUTO_SYNC = "AUTO_SYNC";

    protected Uri fileUri;
    protected String deckName;
    protected String mimeType;
    protected Long fileLastModifiedAt;
    protected FileSynced fileSynced;
    protected FileSyncDeckDatabase deckDb;
    protected ExceptionHandler exceptionHandler = new ExceptionHandler();

    public SyncExcelToDeckWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams
    ) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        try {
            Data inputData = getInputData();
            deckName = inputData.getString(DECK_NAME);
            deckDb = getDeckDB(deckName);
            fileUri = Uri.parse(inputData.getString(FILE_URI));
            fileSynced = findOrCreateFileSynced(fileUri.toString());
            fileSynced.setAutoSync(inputData.getBoolean(AUTO_SYNC, false));

            askPermissions(fileUri);
            InputStream isImportedFile = openExcelFile(fileUri);

            SyncExcelToDeck syncExcelToDeck = new SyncExcelToDeck(getApplicationContext());
            syncExcelToDeck.syncExcelFile(deckName, fileSynced, isImportedFile, mimeType, fileLastModifiedAt);
            syncExcelToDeck.commitChanges(
                    fileSynced,
                    getApplicationContext()
                            .getContentResolver()
                            .openOutputStream(fileUri)
            );

            showSuccessNotification();
            return Result.success();
        } catch (Exception e) {
            showExceptionDialog(e);
            return Result.failure();
        }
    }

    protected FileSynced findOrCreateFileSynced(String fileUri) {
        FileSynced fileSynced = deckDb.fileSyncedDao().findByUri(fileUri);
        if (fileSynced == null) {
            fileSynced = new FileSynced();
            fileSynced.setUri(fileUri);
        }
        return fileSynced;
    }

    protected void askPermissions(Uri uri) {
        int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        getApplicationContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }

    /**
     * https://developer.android.com/training/data-storage/shared/documents-files
     */
    protected InputStream openExcelFile(Uri uri) throws FileNotFoundException {
        try (Cursor cursor = getApplicationContext().getContentResolver()
                .query(uri, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                mimeType = cursor.getString(
                        cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                ).toLowerCase();
                fileLastModifiedAt = cursor.getLong(
                        cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                );
            }
        }
        return getApplicationContext().getContentResolver().openInputStream(uri);
    }

    protected void showSuccessNotification() {
        (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                getApplicationContext(),
                String.format("The deck \"%s\" has been synced with the file.", deckName),
                Toast.LENGTH_LONG
                ).show()
        );
    }

    protected void showExceptionDialog(Exception e) {
        exceptionHandler.handleException(
                e, ((FlashCardsApp) getApplicationContext()).getActiveActivity(),
                SyncExcelToDeckWorker.class.getSimpleName()
        );
    }

    protected FileSyncDeckDatabase getDeckDB(@NonNull String deckName) {
        return FileSyncDatabaseUtil.getInstance(getApplicationContext()).getDeckDatabase(deckName);
    }
}
