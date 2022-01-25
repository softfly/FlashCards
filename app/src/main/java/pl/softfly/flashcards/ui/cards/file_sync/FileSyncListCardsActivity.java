package pl.softfly.flashcards.ui.cards.file_sync;

import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLS;
import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLSX;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.entity.DeckConfig;
import pl.softfly.flashcards.filesync.FileSync;
import pl.softfly.flashcards.tasks.LongTasksExecutor;
import pl.softfly.flashcards.ui.cards.select.SelectListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncListCardsActivity extends SelectListCardsActivity {

    private FileSyncCardRecyclerViewAdapter adapter;

    @Nullable
    private DeckDatabase deckDb;

    private boolean editingLocked = false;

    private ItemTouchHelper itemTouchHelper;

    @Nullable
    private FileSync fileSync = FileSync.getInstance();

    private final ActivityResultLauncher<String[]> syncExcel =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    syncedExcelUri -> {
                        if (syncedExcelUri != null)
                            fileSync.syncFile(getDeckName(), syncedExcelUri, this);
                    }
            );


    private final ActivityResultLauncher<String> exportExcel =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument() {
                        @NonNull
                        @Override
                        public Intent createIntent(@NonNull Context context, @NonNull String input) {
                            return super.createIntent(context, input)
                                    .setType(TYPE_XLSX);
                        }
                    },
                    exportedExcelUri -> {
                        if (exportedExcelUri != null)
                            fileSync.exportFile(getDeckName(), exportedExcelUri, this);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.deckDb = getDeckDatabase();
        checkIfEditingIsLocked();
    }

    @Override
    protected FileSyncCardRecyclerViewAdapter onCreateRecyclerViewAdapter() {
        adapter = new FileSyncCardRecyclerViewAdapter(this, getDeckName());
        setAdapter(adapter);
        return adapter;
    }

    @NonNull
    @Override
    protected ItemTouchHelper.Callback onCreateTouchHelper() {
        return new FileSyncCardTouchHelper(adapter);
    }

    private void checkIfEditingIsLocked() {
        Handler handler = new Handler(Looper.myLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                deckDb.deckConfigAsyncDao().getByKey(DeckConfig.FILE_SYNC_EDITING_BLOCKED_AT)
                        .subscribeOn(Schedulers.io())
                        .doOnError(Throwable::printStackTrace)
                        .doOnSuccess(deckConfig -> {
                            if (!LongTasksExecutor.getInstance().isAlive()) {
                                deckConfig.setValue(null);
                                deckDb.deckConfigDao().update(deckConfig);
                                if (editingLocked) unlockEditing();
                            } else if (!editingLocked) {
                                lockEditing();
                            } else {
                                // Check back in a while
                                handler.postDelayed(this, 1000);
                            }
                        })
                        .doOnEvent((value, error) -> {
                            if (value == null && error == null) {
                                if (editingLocked) {
                                    unlockEditing();
                                }
                            }
                        })
                        .subscribe();

            }
        });
    }

    public void lockEditing() {
        editingLocked = true;
        itemTouchHelper = adapter.getTouchHelper();
        runOnUiThread(() -> {
            findViewById(R.id.editingLocked).setVisibility(View.VISIBLE);
            itemTouchHelper.attachToRecyclerView(null);
        });
        refreshMenuOnAppBar();
        adapter.setTouchHelper(null);
    }

    public void unlockEditing() {
        runOnUiThread(() -> {
            findViewById(R.id.editingLocked).setVisibility(View.INVISIBLE);
            itemTouchHelper.attachToRecyclerView(getRecyclerView());
        });
        refreshMenuOnAppBar();
        adapter.setTouchHelper(itemTouchHelper);
        editingLocked = false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync_excel:
                syncExcel.launch(new String[] {TYPE_XLS, TYPE_XLSX});
                return true;
            case R.id.export_excel:
                exportExcel.launch(getDeckName());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        return isEditingUnlocked() && super.onCreateOptionsMenu(menu);
    }

    @Nullable
    protected DeckDatabase getDeckDatabase() {
        return AppDatabaseUtil
                .getInstance(getBaseContext())
                .getDeckDatabase(getDeckName());
    }

    public boolean isEditingUnlocked() {
        return !editingLocked;
    }
}
