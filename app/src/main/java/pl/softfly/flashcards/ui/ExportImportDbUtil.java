package pl.softfly.flashcards.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.storage.StorageDb;

/**
 * @author Grzegorz Ziemski
 */
public class ExportImportDbUtil {

    protected String path;

    protected Fragment fragment;

    protected ActivityResultLauncher<String> exportDbLauncher;

    protected ActivityResultLauncher<String[]> importDbLauncher;

    public ExportImportDbUtil(Fragment fragment) {
        this.fragment = fragment;
        exportDbLauncher = initExportDbLauncher();
        importDbLauncher = initImportDbLauncher();
    }

    protected ActivityResultLauncher<String> initExportDbLauncher() {
        return fragment.registerForActivityResult(
                new ActivityResultContracts.CreateDocument() {
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull String input) {
                        return super.createIntent(context, input)
                                .setType("application/vnd.sqlite3");
                    }
                },
                exportedDbUri -> {
                    if (exportedDbUri != null)
                        exportDb(exportedDbUri, path);
                }
        );
    }

    protected ActivityResultLauncher<String[]> initImportDbLauncher() {
        return fragment.registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                importedDbUri -> {
                    if (importedDbUri != null)
                        importDb(path, importedDbUri);
                }
        );
    }

    protected void exportDb(Uri exportToUri, @NonNull String dbPath) {
        Completable.fromRunnable(() -> {
                    DeckDatabaseUtil
                            .getInstance(fragment.getActivity().getApplicationContext())
                            .closeDatabase(dbPath);

                    try (OutputStream out = fragment.getActivity()
                            .getContentResolver()
                            .openOutputStream(exportToUri)) {
                        try (FileInputStream in = new FileInputStream(dbPath)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                FileUtils.copy(in, out);
                            } else {
                                FileChannel inChannel = in.getChannel();
                                FileChannel outChannel = ((FileOutputStream) out).getChannel();
                                inChannel.transferTo(0, inChannel.size(), outChannel);
                            }
                        }
                    } catch (Exception e) {
                        onErrorExportDb(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, this::onErrorExportDb);
    }

    protected void onErrorExportDb(Throwable e) {
        getExceptionHandler().handleException(
                e, fragment.getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while exporting DB."
        );
    }

    @SuppressLint("Range")
    protected void importDb(@NonNull String importToFolder, Uri importedDbUri) {
        Completable.fromRunnable(() -> {
                    StorageDb storageDb = DeckDatabaseUtil
                            .getInstance(fragment.getActivity().getApplicationContext())
                            .getStorageDb();
                    try {
                        String deckDbPath = null;
                        try (Cursor cursor = fragment.getActivity().getContentResolver()
                                .query(importedDbUri, null, null, null)) {
                            if (cursor != null && cursor.moveToFirst()) {
                                deckDbPath = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                deckDbPath = storageDb.findFreePath(importToFolder, deckDbPath);
                            }
                        }
                        InputStream in = fragment.getActivity().getContentResolver().openInputStream(importedDbUri);
                        FileOutputStream out = new FileOutputStream(deckDbPath);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FileUtils.copy(in, out);
                        } else {
                            FileChannel inChannel = ((FileInputStream) in).getChannel();
                            FileChannel outChannel = out.getChannel();
                            inChannel.transferTo(0, inChannel.size(), outChannel);
                        }
                        in.close();
                        out.close();
                        //adapter.refreshItems();
                    } catch (IOException e) {
                        onErrorImportDb(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, this::onErrorImportDb);
    }

    protected void onErrorImportDb(Throwable e) {
        getExceptionHandler().handleException(
                e, fragment.getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while importing DB."
        );
    }

    public void launchExportDb(String dbPath) {
        path = dbPath;
        exportDbLauncher.launch(getDeckName(dbPath));
    }

    public void launchImportDb(String importToFolder) {
        path = importToFolder;
        importDbLauncher.launch(new String[]{
                "application/vnd.sqlite3",
                "application/octet-stream"
        });
    }

    protected String getDeckName(String dbPath) {
        return dbPath.substring(dbPath.lastIndexOf("/") + 1);
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
