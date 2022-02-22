package pl.softfly.flashcards.filesync.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import java.io.InputStream;

import pl.softfly.flashcards.filesync.algorithms.ExportExcelToDeck;

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
            Data inputData = getInputData();
            deckName = inputData.getString(DECK_NAME);
            deckDb = getDeckDB(deckName);
            fileUri = Uri.parse(inputData.getString(FILE_URI));
            fileSynced = findOrCreateFileSynced(fileUri.toString());
            fileSynced.setAutoSync(inputData.getBoolean(AUTO_SYNC, false));

            askPermissions(fileUri);
            InputStream isImportedFile = openExcelFile(fileUri);

            ExportExcelToDeck exportExcelToDeck = new ExportExcelToDeck(getApplicationContext());
            exportExcelToDeck.syncExcelFile(deckName, fileSynced, isImportedFile, mimeType, fileLastModifiedAt);
            exportExcelToDeck.commitChanges(
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

    @Override
    protected void showSuccessNotification() {
        (new Handler(Looper.getMainLooper())).post(() -> Toast.makeText(
                getApplicationContext(),
                String.format("The deck \"%s\" has been exported to the file.", deckName),
                Toast.LENGTH_LONG
                ).show()
        );
    }
}