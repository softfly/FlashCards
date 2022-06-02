package pl.softfly.flashcards.filesync.worker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

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

    public static final String DECK_DB_PATH = "DECK_DB_PATH";
    public static final String FILE_URI = "FILE_URI";
    public static final String AUTO_SYNC = "AUTO_SYNC";

    protected Uri fileUri;
    @Nullable
    protected String deckDbPath;
    protected String mimeType;
    protected Long fileLastModifiedAt;
    @Nullable
    protected FileSyncDeckDatabase deckDb;
    protected ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();

    public SyncExcelToDeckWorker(
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
            deckDbPath = inputData.getString(DECK_DB_PATH);
            Objects.requireNonNull(deckDbPath);
            fileUri = Uri.parse(inputData.getString(FILE_URI));
            Objects.requireNonNull(fileUri);

            deckDb = getDeckDB(deckDbPath);
            FileSynced fileSynced = findOrCreateFileSynced(fileUri.toString());
            fileSynced.setAutoSync(inputData.getBoolean(AUTO_SYNC, false));

            askPermissions(fileUri);

            SyncExcelToDeck syncExcelToDeck = new SyncExcelToDeck(getApplicationContext());
            try (InputStream isFile = openFileToRead(fileUri)) {
                syncExcelToDeck.syncExcelFile(deckDbPath, fileSynced, isFile, mimeType, fileLastModifiedAt);
            }
            try (OutputStream outFile = openFileToWrite(fileUri)) {
                syncExcelToDeck.commitChanges(fileSynced, outFile);
            }

            showSuccessNotification(syncExcelToDeck);
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
    @SuppressLint("Range")
    protected InputStream openFileToRead(Uri uri) throws FileNotFoundException {
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

    protected OutputStream openFileToWrite(Uri uri) throws FileNotFoundException {
        return getApplicationContext().getContentResolver().openOutputStream(uri);
    }

    protected void showSuccessNotification(SyncExcelToDeck syncExcelToDeck) {
        (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                        getApplicationContext(),
                        (new SyncSuccessNotificationFactory())
                                .create(
                                        syncExcelToDeck.getDeckAdded(),
                                        syncExcelToDeck.getDeckUpdated(),
                                        syncExcelToDeck.getDeckDeleted(),
                                        syncExcelToDeck.getFileAdded(),
                                        syncExcelToDeck.getFileUpdated(),
                                        syncExcelToDeck.getFileDeleted()
                                ),
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

    @Nullable
    protected FileSyncDeckDatabase getDeckDB(@NonNull String deckName) {
        return FileSyncDatabaseUtil.getInstance(getApplicationContext()).getDeckDatabase(deckName);
    }

    @NonNull
    private String getDeckName() {
        return deckDbPath.substring(deckDbPath.lastIndexOf("/") + 1);
    }

    public static class SyncSuccessNotificationFactory {

        public String create(
                int deckAdded,
                int deckUpdated,
                int deckDeleted,
                int fileAdded,
                int fileUpdated,
                int fileDeleted
        ) {
            StringBuilder sb = new StringBuilder(String.format(
                    "Deck: |%d added|| and ||%d updated|| and ||%d deleted|\n" +
                            "File: |%d added|| and ||%d updated|| and ||%d deleted|",
                    deckAdded, deckUpdated, deckDeleted, fileAdded, fileUpdated, fileDeleted
            ));

            int fromIndex = processLine(sb, 0, deckAdded, deckUpdated, deckDeleted);
            processLine(sb, fromIndex, fileAdded, fileUpdated, fileDeleted);
            return sb.toString();
        }

        protected int processLine(
                StringBuilder sb,
                int fromIndex,
                int added,
                int updated,
                int deleted
        ) {
            if (added == 0) {
                fromIndex = removePos(sb, fromIndex);
            } else {
                fromIndex = removeVerticalBar(sb, 0);
                fromIndex = removeVerticalBar(sb, fromIndex);
            }

            if (updated == 0 && deleted == 0 || added == 0) {
                removeAnd(sb, fromIndex);
            } else {
                fromIndex = removeVerticalBar(sb, 0);
                fromIndex = removeVerticalBar(sb, fromIndex);
            }

            if (updated == 0 && (added != 0 || deleted != 0)) {
                fromIndex = removePos(sb, fromIndex);
            } else {
                fromIndex = removeVerticalBar(sb, fromIndex);
                fromIndex = removeVerticalBar(sb, fromIndex);
            }

            if (updated == 0 || deleted == 0) {
                removeAnd(sb, fromIndex);
            } else {
                fromIndex = removeVerticalBar(sb, fromIndex);
                fromIndex = removeVerticalBar(sb, fromIndex);
            }

            if (deleted == 0) {
                fromIndex = removePos(sb, fromIndex);
            } else {
                fromIndex = removeVerticalBar(sb, fromIndex);
                fromIndex = removeVerticalBar(sb, fromIndex);
            }

            return fromIndex;
        }

        protected int removePos(StringBuilder sb, int fromIndex) {
            int start = sb.indexOf("|", fromIndex);
            int end = sb.indexOf("|", start + 1);
            sb.replace(start, end + 1, "");
            return start;
        }

        protected int removeAnd(StringBuilder sb, int fromIndex) {
            int start = sb.indexOf("|", fromIndex);
            int end = sb.indexOf("|", start + 1);
            sb.replace(start, end + 1, "");
            return end;
        }

        protected int removeVerticalBar(StringBuilder sb, int fromIndex) {
            int start = sb.indexOf("|", fromIndex);
            sb.replace(start, start + 1, "");
            return start;
        }

    }
}
