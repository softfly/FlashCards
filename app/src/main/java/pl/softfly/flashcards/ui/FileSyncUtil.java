package pl.softfly.flashcards.ui;

import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLS;
import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLSX;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import pl.softfly.flashcards.filesync.FileSync;

/**
 * @author Grzegorz Ziemski
 */
public class FileSyncUtil {

    protected final FileSync fileSync = FileSync.getInstance();

    protected String path;

    protected AppCompatActivity activity;

    protected ActivityResultLauncher<String[]> syncExcelLauncher;

    protected ActivityResultLauncher<String> exportExcelLauncher;

    protected ActivityResultLauncher<String[]> importExcelLauncher;

    public FileSyncUtil(AppCompatActivity activity) {
        this.activity = activity;
        syncExcelLauncher = initSyncExcelLauncher();
        exportExcelLauncher = initExportExcelLauncher();
        importExcelLauncher = initImportExcelLauncher();
    }

    protected ActivityResultLauncher<String[]> initSyncExcelLauncher() {
        return activity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                syncedExcelUri -> {
                    if (syncedExcelUri != null)
                        fileSync.syncFile(path, syncedExcelUri, activity);
                }
        );
    }

    protected ActivityResultLauncher<String> initExportExcelLauncher() {
        return activity.registerForActivityResult(
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
                        fileSync.exportFile(path, exportedExcelUri, activity);
                }
        );
    }

    protected ActivityResultLauncher<String[]> initImportExcelLauncher() {
        return activity.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                importedExcelUri -> {
                    if (importedExcelUri != null)
                        fileSync.importFile(
                                path,
                                importedExcelUri,
                                activity);
                }
        );
    }

    public void launchSyncFile(String deckDbPath) {
        this.path = deckDbPath;
        syncExcelLauncher.launch(new String[]{TYPE_XLS, TYPE_XLSX});
    }

    public void launchExportToFile(String deckDbPath) {
        this.path = deckDbPath;
        exportExcelLauncher.launch(getDeckName(deckDbPath) + ".xlsx");
    }

    public void launchImportExcel(String importToFolder) {
        this.path = importToFolder;
        importExcelLauncher.launch(new String[]{TYPE_XLS, TYPE_XLSX});
    }

    protected String getDeckName(String dbPath) {
        return dbPath.substring(dbPath.lastIndexOf("/") + 1)
                .replace(".db", "");
    }
}