package pl.softfly.flashcards.ui.deck;

import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLS;
import static pl.softfly.flashcards.filesync.FileSync.TYPE_XLSX;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.Config;
import pl.softfly.flashcards.CreateSampleDeck;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabase;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.storage.StorageDb;
import pl.softfly.flashcards.entity.AppConfig;
import pl.softfly.flashcards.filesync.FileSync;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.IconWithTextInTopbarActivity;
import pl.softfly.flashcards.ui.app.settings.AppSettingsActivity;
import pl.softfly.flashcards.ui.deck.folder.FolderDeckRecyclerViewAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class ListDecksActivity extends IconWithTextInTopbarActivity {

    protected File currentFolder;
    private ListDecksActivity listDecksActivity;
    private final ActivityResultLauncher<String[]> importExcel =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    importedExcelUri -> {
                        if (importedExcelUri != null)
                            FileSync.getInstance().importFile(
                                    currentFolder.getPath(),
                                    importedExcelUri,
                                    listDecksActivity);
                    }
            );
    private DeckRecyclerViewAdapter adapter;
    private final ActivityResultLauncher<String[]> importDbDeck =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    importedDbUri -> {
                        if (importedDbUri != null)
                            importDbDeck(currentFolder.getPath(), importedDbUri);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initDarkMode();
        super.onCreate(savedInstanceState);
        this.listDecksActivity = this;
        setContentView(R.layout.activity_list_decks);
        initCurrentFolder();
        initRecyclerView();
        adapter.loadItems(currentFolder);
        askPermissionManageExternalStorage();

        //UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        //uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);


        try {
            (new CreateSampleDeck()).create(
                    getApplicationContext(),
                    () -> adapter.loadItems(currentFolder)
            );
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(this.getSupportFragmentManager(), "CreateDeck");
        }
    }

    @Override
    protected void onResume() {
        initDarkMode();
        super.onResume();
    }

    /**
     * It is not needed elsewhere, because
     * 1) this is the first activity
     * 2) after changing the app settings, it comes back to this activity
     */
    protected void initDarkMode() {
        getAppDatabase().appConfigAsync().findByKey(AppConfig.DARK_MODE)
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .subscribe(appConfig -> {
                    switch (appConfig.getValue()) {
                        case AppConfig.DARK_MODE_ON:
                            runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));
                            break;
                        case AppConfig.DARK_MODE_OFF:
                            runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO));
                            break;
                        default:
                            runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
                            break;
                    }
                });
    }

    @Nullable
    protected AppDatabase getAppDatabase() {
        return AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getAppDatabase();
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.deck_list_view);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(), DividerItemDecoration.VERTICAL
                )
        );
        adapter = new FolderDeckRecyclerViewAdapter(this, currentFolder);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void initCurrentFolder() {
        currentFolder = new File(
                AppDatabaseUtil
                        .getInstance(getApplicationContext())
                        .getStorageDb()
                        .getDbFolder()
        );
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        menu.add(0, R.id.new_deck, 1,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_outline_add_24),
                        "New deck"
                ));
        menu.add(0, R.id.new_folder, 2,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_baseline_create_new_folder_24),
                        "New folder"
                ));
        menu.add(0, R.id.import_excel, 3,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_round_file_download_24),
                        "Import Excel"
                ));
        menu.add(0, R.id.import_db, 4,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_round_file_download_24),
                        "Import DB"
                ));
        menu.add(0, R.id.settings, 5,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_baseline_settings_24),
                        "Settings"
                ));
        getMenuInflater().inflate(R.menu.menu_list_decks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_deck: {
                DialogFragment dialog = new CreateDeckDialog(currentFolder);
                dialog.show(this.getSupportFragmentManager(), "CreateDeck");
                return true;
            }
            case R.id.import_excel:
                importExcel.launch(new String[]{TYPE_XLS, TYPE_XLSX});
                return true;
            case R.id.import_db:
                importDbDeck.launch(new String[]{
                        "application/vnd.sqlite3",
                        "application/octet-stream"
                });
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, AppSettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void askPermissionManageExternalStorage() {
        Config config = Config.getInstance(getApplicationContext());
        if (config.isDatabaseExternalStorage() || config.isTestFilesExternalStorage()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }
    }

    @SuppressLint("Range")
    protected void importDbDeck(@NonNull String importToFolder, Uri importedDbUri) {
        StorageDb storageDb = AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getStorageDb();
        try {
            String deckName = null;
            try (Cursor cursor = getContentResolver()
                    .query(importedDbUri, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    deckName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    deckName = storageDb.findFreeName(importToFolder, deckName);
                }
            }
            InputStream in = getContentResolver().openInputStream(importedDbUri);
            FileOutputStream out = new FileOutputStream(importToFolder + "/" + deckName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(in, out);
            } else {
                FileChannel inChannel = ((FileInputStream) in).getChannel();
                FileChannel outChannel = out.getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
            in.close();
            out.close();
            adapter.loadItems(currentFolder);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog eDialog = new ExceptionDialog(e);
            eDialog.show(getSupportFragmentManager(), "ImportDbDeck");
        }
    }

    public void loadDecks() {
        adapter.loadItems(currentFolder);
    }
}