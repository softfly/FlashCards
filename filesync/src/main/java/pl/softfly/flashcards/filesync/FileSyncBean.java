package pl.softfly.flashcards.filesync;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.entity.DeckConfig;
import pl.softfly.flashcards.filesync.db.FileSyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.FileSyncDeckDatabase;
import pl.softfly.flashcards.filesync.entity.FileSynced;
import pl.softfly.flashcards.filesync.ui.EditingDeckLockedDialog;
import pl.softfly.flashcards.filesync.ui.SetUpAutoSyncFileDialog;
import pl.softfly.flashcards.filesync.worker.ExportExcelFromDeckWorker;
import pl.softfly.flashcards.filesync.worker.ImportExcelToDeckWorker;
import pl.softfly.flashcards.filesync.worker.SyncExcelToDeckWorker;
import pl.softfly.flashcards.ui.cards.file_sync.FileSyncListCardsActivity;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

/**
 * 1. Check that the deck is not being edited by another task.
 * 1.1 If Yes, display a message that deck editing is blocked and end use case.
 * 2. Check the deck is auto-sync with this file.
 * If Yes, skip steps 3,4.
 * 3. Ask if the deck should auto-sync with this file.
 * If No, skip step 4.
 * 4. Set up the file to auto-sync in the future.
 *
 * @author Grzegorz Ziemski
 */
public class FileSyncBean implements FileSync {

    @Nullable
    private FileSyncDeckDatabase deckDb;

    private ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();

    /**
     * SE Synchronize deck with excel file.
     */
    @Override
    public void syncFile(
            @NonNull String deckName,
            @NonNull Uri uri,
            @NonNull FileSyncListCardsActivity listCardsActivity
    ) {
        if (deckDb == null) {
            deckDb = FileSyncDatabaseUtil
                    .getInstance(listCardsActivity.getApplicationContext())
                    .getDeckDatabase(deckName);
        }
        checkIfEditingIsLocked(listCardsActivity, () ->
                deckDb.fileSyncedDao().findByUriAsync(uri.toString())
                        .subscribeOn(Schedulers.io())
                        .doOnError(Throwable::printStackTrace)
                        .doOnSuccess(fileSynced -> setUpAutoSync(
                                fileSynced,
                                listCardsActivity,
                                () -> syncWorkRequest(deckName, fileSynced, listCardsActivity)

                        ))
                        .doOnEvent((value, error) -> {
                            if (value == null && error == null) {
                                FileSynced fileSynced = new FileSynced();
                                fileSynced.setUri(uri.toString());
                                setUpAutoSync(
                                        fileSynced,
                                        listCardsActivity,
                                        () -> syncWorkRequest(deckName, fileSynced, listCardsActivity)
                                );
                            }
                        })
                        .subscribe());
    }

    protected void syncWorkRequest(
            String deckName,
            FileSynced fileSynced,
            FileSyncListCardsActivity listCardsActivity
    ) {
        WorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(SyncExcelToDeckWorker.class)
                        .setInputData(createInputData(deckName, fileSynced))
                        .addTag(TAG)
                        .build();

        WorkManager workManager = WorkManager.getInstance(listCardsActivity.getApplicationContext());
        workManager.enqueue(syncWorkRequest);

        workManager.getWorkInfoByIdLiveData(syncWorkRequest.getId())
                .observe(listCardsActivity, workInfo -> {
                    if (workInfo.getState() != null
                            && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        if (listCardsActivity
                                .getLifecycle()
                                .getCurrentState()
                                .isAtLeast(Lifecycle.State.RESUMED)
                        ) {
                            listCardsActivity.onRestart();
                        }
                    }
                });
    }

    protected Data createInputData(String deckName, FileSynced fileSynced) {
        return new Data.Builder()
                .putString(SyncExcelToDeckWorker.DECK_NAME, deckName)
                .putString(SyncExcelToDeckWorker.FILE_URI, fileSynced.getUri())
                .putBoolean(SyncExcelToDeckWorker.AUTO_SYNC, fileSynced.isAutoSync())
                .build();
    }

    /**
     * IE Create a deck from an imported Excel file.
     */
    @Override
    public void importFile(@NonNull Uri uri, @NonNull ListDecksActivity listDecksActivity) {
        FileSynced fileSynced = new FileSynced();
        fileSynced.setUri(uri.toString());
        setUpAutoSync(
                fileSynced,
                listDecksActivity,
                () -> importWorkRequest(fileSynced, listDecksActivity)
        );
    }

    protected void importWorkRequest(
            FileSynced fileSynced,
            ListDecksActivity listDecksActivity
    ) {
        WorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(ImportExcelToDeckWorker.class)
                        .setInputData(
                                new Data.Builder()
                                        .putString(ImportExcelToDeckWorker.FILE_URI, fileSynced.getUri())
                                        .putBoolean(ImportExcelToDeckWorker.AUTO_SYNC, fileSynced.isAutoSync())
                                        .build())
                        .addTag(TAG)
                        .build();

        WorkManager workManager = WorkManager.getInstance(listDecksActivity.getApplicationContext());
        workManager.enqueue(syncWorkRequest);
        workManager.getWorkInfoByIdLiveData(syncWorkRequest.getId())
                .observe(listDecksActivity, workInfo -> {
                    if (workInfo.getState() != null
                            && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        if (listDecksActivity
                                .getLifecycle()
                                .getCurrentState()
                                .isAtLeast(Lifecycle.State.RESUMED)
                        ) {
                            listDecksActivity.loadDecks();
                        }
                    }
                });
    }

    /**
     * EE Create a new Excel file from the exported deck.
     */
    @Override
    public void exportFile(
            @NonNull String deckName,
            @NonNull Uri uri,
            @NonNull FileSyncListCardsActivity listCardsActivity
    ) {
        if (deckDb == null) {
            deckDb = FileSyncDatabaseUtil
                    .getInstance(listCardsActivity.getApplicationContext())
                    .getDeckDatabase(deckName);
        }
        checkIfEditingIsLocked(listCardsActivity, () -> {
            FileSynced fileSynced = new FileSynced();
            fileSynced.setUri(uri.toString());
            setUpAutoSync(
                    fileSynced,
                    listCardsActivity,
                    () -> exportWorkRequest(deckName, fileSynced, listCardsActivity.getApplicationContext())
            );
        });
    }

    protected void exportWorkRequest(String deckName, FileSynced fileSynced, Context context) {
        WorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(ExportExcelFromDeckWorker.class)
                        .setInputData(createInputData(deckName, fileSynced))
                        .addTag(TAG)
                        .build();
        WorkManager
                .getInstance(context)
                .enqueue(syncWorkRequest);
    }

    /**
     * 1. Check that the deck is not being edited by another task.
     */
    protected void checkIfEditingIsLocked(@NonNull AppCompatActivity activity, @NonNull Runnable andThen) {
        deckDb.deckConfigAsyncDao().getLongByKey(DeckConfig.FILE_SYNC_EDITING_BLOCKED_AT)
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(blockedAt -> {
                    WorkManager workManager = WorkManager.getInstance(activity.getApplicationContext());
                    ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfosByTag(FileSync.TAG);
                    if (statuses.get().isEmpty()) {
                        andThen.run();
                    } else {
                        // 1.1 If Yes, display a message that deck editing is blocked and end use case.
                        new EditingDeckLockedDialog()
                                .show(activity.getSupportFragmentManager(), "EditingDeckLocked");
                    }
                })
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        andThen.run();
                    }
                })
                .subscribe(blockedAt -> {}, e -> exceptionHandler.handleException(
                        e, activity.getSupportFragmentManager(),
                        FileSyncBean.class.getSimpleName(),
                        "Error while exporting or syncing cards.",
                        (dialog, which) -> activity.onBackPressed()
                ));
    }

    protected void setUpAutoSync(
            @NonNull FileSynced fileSynced,
            @NonNull AppCompatActivity activity,
            @NonNull Runnable andThen
    ) {
        // 2. Check the deck is auto-sync with this file.
        if (fileSynced.isAutoSync()) {
            andThen.run();
        } else {
            // 3. Ask if the deck should auto-sync with this file.
            new SetUpAutoSyncFileDialog(fileSynced, activity, andThen)
                    .show(activity.getSupportFragmentManager(), "SetUpAutoSync");
        }
    }
}
