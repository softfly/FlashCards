package pl.softfly.flashcards.filesync;

import android.content.DialogInterface;
import android.net.Uri;
import android.widget.Toast;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.filesync.db.SyncDatabaseUtil;
import pl.softfly.flashcards.filesync.db.SyncDeckDatabase;
import pl.softfly.flashcards.filesync.entity.FileSynced;
import pl.softfly.flashcards.filesync.task.ImportExcelToDeckTask;
import pl.softfly.flashcards.filesync.task.SyncExcelToDeckTask;
import pl.softfly.flashcards.filesync.ui.SetUpAutoSyncFileDialog;
import pl.softfly.flashcards.tasks.LongTasksExecutor;
import pl.softfly.flashcards.ui.cards.ListCardsActivity;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

public class FileSyncBean implements FileSync {

    private SyncDeckDatabase deckDb;

    @Override
    public void syncFile(String deckName,
                         Uri uri,
                         ListCardsActivity listCardsActivity
    ) {
        if (deckDb == null) {
            deckDb = SyncDatabaseUtil
                    .getInstance(listCardsActivity.getApplicationContext())
                    .getDeckDatabase(deckName);
        }

        deckDb.fileSyncedDao().findByUriAsync(uri.toString())
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(fileSynced -> syncFileSetUpAutoSync(deckName, fileSynced, uri, listCardsActivity))
                .doOnEvent((value, error) -> {
                    if (value == null && error == null) {
                        FileSynced fileSynced = new FileSynced();
                        fileSynced.setUri(uri.toString());
                        syncFileSetUpAutoSync(deckName, fileSynced, uri, listCardsActivity);
                    }
                })
                .subscribe();
    }

    protected void syncFileSetUpAutoSync(
            String deckName,
            FileSynced fileSynced,
            Uri uri,
            ListCardsActivity listCardsActivity
    ) {
        //listCardsActivity.runOnUiThread(() -> { }))

        if (fileSynced.isAutoSync()) {
            LongTasksExecutor.getInstance().doTask(
                    new SyncExcelToDeckTask(deckName, fileSynced, uri, listCardsActivity)
            );
        } else {
            DialogInterface.OnClickListener positiveButton = (dialog, which) -> {
                fileSynced.setAutoSync(true);
                Toast.makeText(
                        listCardsActivity.getApplicationContext(),
                        "Auto-sync has been set up.",
                        Toast.LENGTH_LONG
                ).show();
                LongTasksExecutor.getInstance().doTask(
                        new SyncExcelToDeckTask(deckName, fileSynced, uri, listCardsActivity)
                );
            };

            DialogInterface.OnClickListener negativeButton = (dialog, which) ->
                    LongTasksExecutor.getInstance().doTask(
                            new SyncExcelToDeckTask(deckName, fileSynced, uri, listCardsActivity)
                    );

            new SetUpAutoSyncFileDialog(positiveButton, negativeButton)
                    .show(listCardsActivity.getSupportFragmentManager(), "AutoSyncFile");
        }
    }

    @Override
    public void importFile(Uri uri, ListDecksActivity listDecksActivity) {
        FileSynced fileSynced = new FileSynced();
        fileSynced.setUri(uri.toString());

        DialogInterface.OnClickListener positiveButton = (dialog, which) -> {
            fileSynced.setAutoSync(true);
            Toast.makeText(
                    listDecksActivity.getApplicationContext(),
                    "Auto-sync has been set up.",
                    Toast.LENGTH_LONG
            ).show();
            LongTasksExecutor.getInstance().doTask(
                    new ImportExcelToDeckTask(listDecksActivity, uri)
            );
        };

        DialogInterface.OnClickListener negativeButton = (dialog, which) ->
                LongTasksExecutor.getInstance().doTask(
                        new ImportExcelToDeckTask(listDecksActivity, uri)
                );

        new SetUpAutoSyncFileDialog(positiveButton, negativeButton)
                .show(listDecksActivity.getSupportFragmentManager(), "AutoSyncFile");
    }

}
