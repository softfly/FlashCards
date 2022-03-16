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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.ArrayList;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.Config;
import pl.softfly.flashcards.CreateSampleDeck;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.db.storage.StorageDb;
import pl.softfly.flashcards.entity.Card;
import pl.softfly.flashcards.filesync.FileSync;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.IconWithTextInTopbarActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ListDecksActivity extends IconWithTextInTopbarActivity {

    private final ArrayList<String> deckNames = new ArrayList<>();

    private ListDecksActivity listDecksActivity;

    private DeckRecyclerViewAdapter deckRecyclerViewAdapter;

    private final ActivityResultLauncher<String[]> importExcel =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    importedExcelUri -> {
                        if (importedExcelUri != null)
                            FileSync.getInstance().importFile(importedExcelUri, listDecksActivity);
                    }
            );

    private final ActivityResultLauncher<String[]> importDbDeck =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    importedDbUri -> {
                        if (importedDbUri != null)
                            importDbDeck(importedDbUri);
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.listDecksActivity = this;
        setContentView(R.layout.activity_list_decks);
        initRecyclerView();
        loadDecks();
        askPermissionManageExternalStorage();
        try {
            (new CreateSampleDeck()).create(getApplicationContext(), this::loadDecks);
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog dialog = new ExceptionDialog(e);
            dialog.show(this.getSupportFragmentManager(), "CreateDeck");
        }
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.deck_list_view);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(), DividerItemDecoration.VERTICAL
                )
        );
        deckRecyclerViewAdapter = new DeckRecyclerViewAdapter(this, deckNames);
        recyclerView.setAdapter(deckRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void loadDecks() {
        deckNames.clear();
        deckNames.addAll(AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getStorageDb()
                .listDatabases()
        );
        deckRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.id.new_deck, 1,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_outline_add_24),
                        "New deck"
                ));
        menu.add(0, R.id.import_excel, 2,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_round_file_download_24),
                        "Import Excel"
                ));
        menu.add(0, R.id.import_db, 3,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_round_file_download_24),
                        "Import DB"
                ));
        getMenuInflater().inflate(R.menu.menu_list_decks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_deck:
                DialogFragment dialog = new CreateDeckDialog();
                dialog.show(this.getSupportFragmentManager(), "CreateDeck");
                return true;
            case R.id.import_excel:
                importExcel.launch(new String[]{TYPE_XLS, TYPE_XLSX});
                return true;
            case R.id.import_db:
                importDbDeck.launch(new String[]{
                        "application/vnd.sqlite3",
                        "application/octet-stream"
                });
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
    protected void importDbDeck(Uri importedDbUri) {
        StorageDb storageDb = AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getStorageDb();
        try {
            String deckName = null;
            try (Cursor cursor = getContentResolver()
                    .query(importedDbUri, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    deckName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    deckName = storageDb.findFreeDeckName(deckName);
                }
            }
            InputStream in = getContentResolver().openInputStream(importedDbUri);
            FileOutputStream out = new FileOutputStream(storageDb.getDbPath(deckName));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(in, out);
            } else {
                FileChannel inChannel = ((FileInputStream) in).getChannel();
                FileChannel outChannel = out.getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
            in.close();
            out.close();
            loadDecks();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog eDialog = new ExceptionDialog(e);
            eDialog.show(getSupportFragmentManager(), "ImportDbDeck");
        }
    }
}