package pl.softfly.flashcards.filesync.worker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.FileNotFoundException;
import java.io.InputStream;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.FlashCardsApp;
import pl.softfly.flashcards.filesync.algorithms.ImportExcelToDeck;
import pl.softfly.flashcards.filesync.entity.FileSynced;

/**
 * This class separates the Android API from the algorithm.
 *
 * @author Grzegorz Ziemski
 */
public class ImportExcelToDeckWorker extends Worker {

    public static final String FILE_URI = "FILE_URI";
    public static final String AUTO_SYNC = "AUTO_SYNC";

    private Uri fileUri;

    private String fileName;

    private String mimeType;

    private Long fileLastModifiedAt;

    private FileSynced fileSynced;

    private ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();

    public ImportExcelToDeckWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams
    ) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Data inputData = getInputData();
            fileUri = Uri.parse(inputData.getString(FILE_URI));
            fileSynced = new FileSynced();
            fileSynced.setUri(fileUri.toString());
            fileSynced.setAutoSync(inputData.getBoolean(AUTO_SYNC, false));

            askPermissions(fileUri);
            InputStream isImportedFile = openExcelFile(fileUri);

            ImportExcelToDeck ImportExcelToDeck = new ImportExcelToDeck(getApplicationContext());
            ImportExcelToDeck.importExcelFile(fileName, isImportedFile, mimeType, fileLastModifiedAt);

            showSuccessNotification();
            return Result.success();
        } catch (Exception e) {
            showExceptionDialog(e);
            return Result.failure();
        }
    }

    protected void askPermissions(Uri uri) {
        int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        getApplicationContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
    }

    /**
     * https://developer.android.com/training/data-storage/shared/documents-files
     */
    @SuppressLint("Range")
    protected InputStream openExcelFile(Uri fileUri) throws FileNotFoundException {
        try (Cursor cursor = getApplicationContext().getContentResolver()
                .query(fileUri, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                mimeType = cursor.getString(
                        cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                ).toLowerCase();
                fileLastModifiedAt = cursor.getLong(
                        cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                );
            }
        }
        return getApplicationContext().getContentResolver().openInputStream(fileUri);
    }

    protected void showSuccessNotification() {
        (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                getApplicationContext(),
                String.format("The new deck \"%s\" has been imported from an Excel file.", fileName),
                Toast.LENGTH_LONG
                ).show()
        );
    }

    protected void showExceptionDialog(@NonNull Exception e) {
        exceptionHandler.handleException(
                e, ((FlashCardsApp) getApplicationContext()).getActiveActivity(),
                SyncExcelToDeckWorker.class.getSimpleName()
        );
    }
}
