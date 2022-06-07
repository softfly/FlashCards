package pl.softfly.flashcards.filesync.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import java.io.OutputStream;

import pl.softfly.flashcards.filesync.algorithms.ExportExcelToDeck;
import pl.softfly.flashcards.filesync.entity.FileSynced;

/**
 * This class separates the Android API from the algorithm.
 *
 * @author Grzegorz Ziemski
 */
public class ExportExcelFromDeckWorker extends SyncExcelToDeckWorker {

    public ExportExcelFromDeckWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams
    ) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            readInputData();
            askPermissions(fileUri);
            fdDeckDb = getFsDeckDb(deckDbPath);
            FileSynced fileSynced = prepareFileSynced(fileUri.toString());

            ExportExcelToDeck exportExcelToDeck = new ExportExcelToDeck(getApplicationContext());
            exportExcelToDeck.syncExcelFile(deckDbPath, fileSynced, null, mimeType, 0);
            try (OutputStream outFile = openFileToWrite(fileUri)) {
                exportExcelToDeck.commitChanges(fileSynced, outFile);
            }
            unlockDeckEditing(deckDbPath);
            showSuccessNotification();
            return Result.success();
        } catch (Exception e) {
            showExceptionDialog(e);
            return Result.failure();
        }
    }

    protected void showSuccessNotification() {
        (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                        getApplicationContext(),
                        String.format("The deck \"%s\" has been exported to the file.", getDeckName()),
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    @NonNull
    private String getDeckName() {
        return deckDbPath.substring(deckDbPath.lastIndexOf("/") + 1);
    }
}