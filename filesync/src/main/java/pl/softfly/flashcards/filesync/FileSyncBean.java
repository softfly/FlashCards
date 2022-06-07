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
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.TimeUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;
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

    private final ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();

    @Nullable
    private FileSyncDeckDatabase fsDeckDb;

    @Nullable
    private DeckDatabase deckDb;

    /**
     * SE Synchronize deck with excel file.
     */
    @Override
    public void syncFile(
            @NonNull String deckDbPath,
            @NonNull Uri uri,
            @NonNull FileSyncListCardsActivity listCardsActivity
    ) {
        if (fsDeckDb == null) {
            fsDeckDb = FileSyncDatabaseUtil
                    .getInstance(listCardsActivity.getApplicationContext())
                    .getDeckDatabase(deckDbPath);
        }
        checkIfEditingIsLocked(listCardsActivity, () ->
                fsDeckDb.fileSyncedDao().findByUriAsync(uri.toString())
                        .subscribeOn(Schedulers.io())
                        .doOnError(Throwable::printStackTrace)
                        .doOnSuccess(fileSynced -> setUpAutoSync(
                                fileSynced,
                                listCardsActivity,
                                () -> {
                                    lockDeckEditing(listCardsActivity, deckDbPath);
                                    syncWorkRequest(deckDbPath, fileSynced, listCardsActivity);
                                }
                        ))
                        .doOnEvent((value, error) -> {
                            if (value == null && error == null) {
                                FileSynced fileSynced = new FileSynced();
                                fileSynced.setUri(uri.toString());
                                setUpAutoSync(
                                        fileSynced,
                                        listCardsActivity,
                                        () -> {
                                            lockDeckEditing(listCardsActivity, deckDbPath);
                                            syncWorkRequest(deckDbPath, fileSynced, listCardsActivity);
                                        }
                                );
                            }
                        })
                        .subscribe());
    }

    protected void syncWorkRequest(
            String deckDbPath,
            @NonNull FileSynced fileSynced,
            @NonNull FileSyncListCardsActivity listCardsActivity
    ) {
        WorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(SyncExcelToDeckWorker.class)
                        .setInputData(createInputData(deckDbPath, fileSynced))
                        .addTag(TAG)
                        .build();

        listCardsActivity.runOnUiThread(() -> {
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
        });
    }

    @NonNull
    protected Data createInputData(String deckDbPath, @NonNull FileSynced fileSynced) {
        return new Data.Builder()
                .putString(SyncExcelToDeckWorker.DECK_DB_PATH, deckDbPath)
                .putString(SyncExcelToDeckWorker.FILE_URI, fileSynced.getUri())
                .putBoolean(SyncExcelToDeckWorker.AUTO_SYNC, fileSynced.isAutoSync())
                .build();
    }

    /**
     * IE Create a deck from an imported Excel file.
     */
    @Override
    public void importFile(
            @NonNull String importToFolderPath,
            @NonNull Uri uri,
            @NonNull ListDecksActivity listDecksActivity
    ) {
        FileSynced fileSynced = new FileSynced();
        fileSynced.setUri(uri.toString());
        setUpAutoSync(
                fileSynced,
                listDecksActivity,
                () -> importWorkRequest(importToFolderPath, fileSynced, listDecksActivity)
        );
    }

    protected void importWorkRequest(
            @NonNull String importToFolderPath,
            @NonNull FileSynced fileSynced,
            @NonNull ListDecksActivity listDecksActivity
    ) {
        WorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(ImportExcelToDeckWorker.class)
                        .setInputData(
                                new Data.Builder()
                                        .putString(ImportExcelToDeckWorker.IMPORT_TO_FOLDER_PATH, importToFolderPath)
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
            @NonNull String deckDbPath,
            @NonNull Uri uri,
            @NonNull FileSyncListCardsActivity listCardsActivity
    ) {
        if (fsDeckDb == null) {
            fsDeckDb = FileSyncDatabaseUtil
                    .getInstance(listCardsActivity.getApplicationContext())
                    .getDeckDatabase(deckDbPath);
        }
        checkIfEditingIsLocked(listCardsActivity, () -> {
            FileSynced fileSynced = new FileSynced();
            fileSynced.setUri(uri.toString());
            setUpAutoSync(
                    fileSynced,
                    listCardsActivity,
                    () -> {
                        lockDeckEditing(listCardsActivity, deckDbPath);
                        exportWorkRequest(deckDbPath, fileSynced, listCardsActivity.getApplicationContext());
                    }
            );
        });
    }

    protected void exportWorkRequest(
            String deckDbPath,
            @NonNull FileSynced fileSynced,
            @NonNull Context context
    ) {
        WorkRequest syncWorkRequest =
                new OneTimeWorkRequest.Builder(ExportExcelFromDeckWorker.class)
                        .setInputData(createInputData(deckDbPath, fileSynced))
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
        fsDeckDb.deckConfigAsyncDao().getLongByKey(DeckConfig.FILE_SYNC_EDITING_BLOCKED_AT)
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
                .subscribe(blockedAt -> {
                }, e -> exceptionHandler.handleException(
                        e, activity.getSupportFragmentManager(),
                        FileSyncBean.class.getSimpleName(),
                        "Error while exporting or syncing cards.",
                        (dialog, which) -> activity.onBackPressed()
                ));
    }

    protected void lockDeckEditing(Context context, String deckDbPath) {
        if (deckDb == null) {
            // The db connector must be the same as in the UI app,
            // otherwise LiveData in the UI will not work.
            deckDb = AppDatabaseUtil
                    .getInstance(context)
                    .getDeckDatabase(deckDbPath);
        }
        deckDb.deckConfigAsyncDao()
                .getByKey(DeckConfig.FILE_SYNC_EDITING_BLOCKED_AT)
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(deckConfig -> {
                    deckConfig.setValue(Long.toString(TimeUtil.getNowEpochSec()));
                    deckDb.deckConfigAsyncDao().update(deckConfig).subscribe();
                })
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        DeckConfig deckConfig = new DeckConfig();
                        deckConfig.setKey(DeckConfig.FILE_SYNC_EDITING_BLOCKED_AT);
                        deckConfig.setValue(Long.toString(TimeUtil.getNowEpochSec()));
                        deckDb.deckConfigAsyncDao().insert(deckConfig).subscribe();
                    }
                })
                .subscribe();
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
