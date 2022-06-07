package pl.softfly.flashcards.ui.cards.file_sync;

import static pl.softfly.flashcards.filesync.FileSync.TAG;
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
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.Config;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.entity.DeckConfig;
import pl.softfly.flashcards.filesync.FileSync;
import pl.softfly.flashcards.ui.cards.select.SelectListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncListCardsActivity extends SelectListCardsActivity {

    protected final ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();
    @NonNull
    private final FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
    @Nullable
    private final FileSync fileSync = FileSync.getInstance();
    private final ActivityResultLauncher<String[]> syncExcel =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    syncedExcelUri -> {
                        if (syncedExcelUri != null)
                            fileSync.syncFile(deckDbPath, syncedExcelUri, this);
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
                            fileSync.exportFile(deckDbPath, exportedExcelUri, this);
                    }
            );
    @Nullable
    private DeckDatabase deckDb;
    private FileSyncCardRecyclerViewAdapter adapter;
    private boolean editingLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deckDb = getDeckDatabase();
        deckDb.deckConfigLiveData().findByKey(DeckConfig.FILE_SYNC_EDITING_BLOCKED_AT)
                .observe(this, deckConfig -> {
                    if (deckConfig != null && deckConfig.getValue() != null) {
                        lockEditing();
                        checkIfEditingIsLocked();
                    } else {
                        unlockEditing();
                    }
                });
    }

    @Override
    protected FileSyncCardRecyclerViewAdapter onCreateRecyclerViewAdapter() {
        adapter = new FileSyncCardRecyclerViewAdapter(this, deckDbPath);
        setAdapter(adapter);
        return adapter;
    }

    @NonNull
    @Override
    protected ItemTouchHelper.Callback onCreateTouchHelper() {
        return new FileSyncCardTouchHelper(adapter);
    }

    /**
     * Unlock deck editing if any worker is working.
     * In a properly running application this code should never be executed.
     * That's just in case a worker fails and doesn't unlock the edit deck.
     */
    private void checkIfEditingIsLocked() {
        Handler handler = new Handler(Looper.myLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                deckDb.deckConfigAsyncDao().getByKey(DeckConfig.FILE_SYNC_EDITING_BLOCKED_AT)
                        .subscribeOn(Schedulers.io())
                        .doOnError(Throwable::printStackTrace)
                        .doOnSuccess(deckConfig -> {
                            WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                            ListenableFuture<List<WorkInfo>> statuses = workManager.getWorkInfos(
                                    WorkQuery.Builder
                                            .fromTags(Arrays.asList(FileSync.TAG))
                                            .addStates(Arrays.asList(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING))
                                            .build()
                            );
                            if (statuses.get().isEmpty()) {
                                deckConfig.setValue(null);
                                deckDb.deckConfigDao().update(deckConfig);
                                if (editingLocked) {
                                    unlockEditing();
                                    if (Config.getInstance(getApplicationContext()).isCrashlyticsEnabled()) {
                                        crashlytics.setCustomKey("tag", TAG);
                                        crashlytics.log("Emergency editing unlock, the worker has failed miserably.");
                                    }
                                }
                            } else if (!editingLocked) {
                                lockEditing();
                            } else if (editingLocked) {
                                handler.postDelayed(this, 5000);
                            }
                        })
                        .doOnEvent((value, error) -> {
                            if (value == null && error == null) {
                                if (editingLocked) {
                                    unlockEditing();
                                }
                            }
                        })
                        .subscribe(deckConfig -> {
                        }, e -> exceptionHandler.handleException(
                                e, getSupportFragmentManager(),
                                FileSyncListCardsActivity.class.getSimpleName() + "_CheckIfEditingIsLocked",
                                (dialog, which) -> onBackPressed()
                        ));
            }
        });
    }

    private void lockEditing() {
        editingLocked = true;
        runOnUiThread(() -> {
            findViewById(R.id.editingLocked).setVisibility(View.VISIBLE);
            setDragSwipeEnabled(false);
        });
        refreshMenuOnAppBar();
    }

    private void unlockEditing() {
        runOnUiThread(() -> {
            findViewById(R.id.editingLocked).setVisibility(View.INVISIBLE);
            setDragSwipeEnabled(true);
        });
        refreshMenuOnAppBar();
        editingLocked = false;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (isEditingUnlocked()) {
            menu.add(0, R.id.sync_excel, 2,
                    menuIconWithText(
                            getDrawableHelper(R.drawable.ic_sharp_sync_24),
                            "Sync with Excel"
                    ));
            menu.add(0, R.id.export_excel, 3,
                    menuIconWithText(
                            getDrawableHelper(R.drawable.ic_round_file_upload_24),
                            "Export to new Excel"
                    ));
            if (!adapter.isShowedRecentlySynced()) {
                menu.add(0, R.id.show_recently_synced, 4,
                        menuIconWithText(
                                getDrawableHelper(R.drawable.ic_round_visibility_24),
                                "Show recently synced"
                        ));
            } else {
                menu.add(0, R.id.hide_recently_synced, 5,
                        menuIconWithText(
                                getDrawableHelper(R.drawable.ic_baseline_visibility_off_24),
                                "Hide recently synced"
                        ));
            }
            return super.onCreateOptionsMenu(menu);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync_excel:
                syncExcel.launch(new String[]{TYPE_XLS, TYPE_XLSX});
                return true;
            case R.id.export_excel:
                exportExcel.launch(getDeckName() + ".xlsx");
                return true;
            case R.id.show_recently_synced:
                adapter.showRecentlySynced();
                return true;
            case R.id.hide_recently_synced:
                adapter.hideRecentlySynced();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private String getDeckName() {
        return deckDbPath.substring(deckDbPath.lastIndexOf("/") + 1);
    }

    @Nullable
    protected DeckDatabase getDeckDatabase() {
        return AppDatabaseUtil
                .getInstance(getBaseContext())
                .getDeckDatabase(deckDbPath);
    }

    public boolean isEditingUnlocked() {
        return !editingLocked;
    }

    protected void setAdapter(FileSyncCardRecyclerViewAdapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
    }

}
