package pl.softfly.flashcards.filesync;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.filesync.db.SyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.SyncDeckDatabase;
import pl.softfly.flashcards.filesync.entity.FileSynced;
import pl.softfly.flashcards.filesync.task.ExportExcelFromDeckTask;
import pl.softfly.flashcards.filesync.task.ImportExcelToDeckTask;
import pl.softfly.flashcards.filesync.task.SyncExcelToDeckTask;
import pl.softfly.flashcards.filesync.ui.SetUpAutoSyncFileDialog;
import pl.softfly.flashcards.tasks.LongTasksExecutor;
import pl.softfly.flashcards.ui.cards.ListCardsActivity;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

/**
 * 1. Check the deck is auto-sync with this file.
 *    If Yes, skip steps 2,3.
 * 2. Ask if the deck should auto-sync with this file.
 *    If No, skip step 3.
 * 3. Set up the file to auto-sync in the future.
 */
public class FileSyncBean implements FileSync {

    @Nullable
    private SyncDeckDatabase deckDb;

    /**
     * SE Synchronize deck with excel file.
     */
    @Override
    public void syncFile(
            @NonNull String deckName,
            @NonNull Uri uri,
            @NonNull ListCardsActivity listCardsActivity
    ) {
        if (deckDb == null) {
            deckDb = SyncDatabaseUtil
                    .getInstance(listCardsActivity.getApplicationContext())
                    .getDeckDatabase(deckName);
        }

        deckDb.fileSyncedDao().findByUriAsync(uri.toString())
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(fileSynced -> setUpAutoSync(
                        fileSynced,
                        listCardsActivity,
                        () -> LongTasksExecutor.getInstance().doTask(
                                new SyncExcelToDeckTask(deckName, fileSynced, uri, listCardsActivity)
                        )
                ))
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        FileSynced fileSynced = new FileSynced();
                        fileSynced.setUri(uri.toString());
                        setUpAutoSync(
                                fileSynced,
                                listCardsActivity,
                                () -> LongTasksExecutor.getInstance().doTask(
                                        new SyncExcelToDeckTask(deckName, fileSynced, uri, listCardsActivity)
                                )
                        );
                    }
                })
                .subscribe();
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
                () -> LongTasksExecutor.getInstance().doTask(
                        new ImportExcelToDeckTask(listDecksActivity, uri)
                )
        );
    }

    /**
     * EE Create a new Excel file from the exported deck.
     */
    @Override
    public void exportFile(
            @NonNull String deckName,
            @NonNull Uri uri,
            @NonNull ListCardsActivity listCardsActivity
    ) {
        FileSynced fileSynced = new FileSynced();
        fileSynced.setUri(uri.toString());
        setUpAutoSync(
                fileSynced,
                listCardsActivity,
                () -> LongTasksExecutor.getInstance().doTask(
                        new ExportExcelFromDeckTask(deckName, fileSynced, uri, listCardsActivity)
                )
        );
    }

    protected void setUpAutoSync(
            FileSynced fileSynced,
            AppCompatActivity activity,
            Runnable andThen
    ) {
        // 1. Check the deck is auto-sync with this file.
        if (fileSynced.isAutoSync()) {
            andThen.run();
        } else {
            // 2. Ask if the deck should auto-sync with this file.
            new SetUpAutoSyncFileDialog(fileSynced, activity, andThen)
                    .show(activity.getSupportFragmentManager(), "SetUpAutoSync");
        }
    }
}
