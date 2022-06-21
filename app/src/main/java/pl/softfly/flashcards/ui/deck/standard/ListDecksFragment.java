package pl.softfly.flashcards.ui.deck.standard;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Objects;

import pl.softfly.flashcards.Config;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.FragmentListDecksBinding;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.storage.StorageDb;
import pl.softfly.flashcards.filesync.FileSync;
import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.IconWithTextInTopbarFragment;
import pl.softfly.flashcards.ui.MainActivity;
import pl.softfly.flashcards.ui.app.settings.AppSettingsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ListDecksFragment extends IconWithTextInTopbarFragment {

    private FragmentListDecksBinding binding;

    private ListDecksFragment listDecksFragment;

    private DeckRecyclerViewAdapter adapter;

    private final ActivityResultLauncher<String[]> importExcelLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    importedExcelUri -> {
                        if (importedExcelUri != null)
                            FileSync.getInstance().importFile(
                                    adapter.getCurrentFolder().getPath(),
                                    importedExcelUri,
                                    listDecksFragment);
                    }
            );

    private final ActivityResultLauncher<String[]> importDbLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    importedDbUri -> {
                        if (importedDbUri != null)
                            importDb(adapter.getCurrentFolder().getPath(), importedDbUri);
                    }
            );

    /* -----------------------------------------------------------------------------------------
     * Fragment methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.listDecksFragment = this;
        adapter = onCreateAdapter();
        askPermissionManageExternalStorage();
        setHasOptionsMenu(true);
    }

    protected DeckRecyclerViewAdapter onCreateAdapter() {
        return new DeckRecyclerViewAdapter((MainActivity) getActivity());
    }

    protected void askPermissionManageExternalStorage() {
        Config config = Config.getInstance(getActivity().getApplicationContext());
        if (config.isDatabaseExternalStorage() || config.isTestFilesExternalStorage()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentListDecksBinding.inflate(inflater, container, false);
        initRecyclerView();
        return binding.getRoot();
    }

    protected void initRecyclerView() {
        RecyclerView recyclerView = binding.deckListView;
        recyclerView.addItemDecoration(
                new DividerItemDecoration(
                        recyclerView.getContext(), DividerItemDecoration.VERTICAL
                )
        );
        recyclerView.setAdapter(Objects.requireNonNull(adapter));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onResume() {
        adapter.refreshItems();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
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
        inflater.inflate(R.menu.menu_list_decks, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_deck: {
                DialogFragment dialog = new CreateDeckDialog(adapter.getCurrentFolder(), (MainActivity) getActivity());
                dialog.show(requireActivity().getSupportFragmentManager(), "CreateDeck");
                return true;
            }
            case R.id.import_excel:
                importExcelLauncher.launch(new String[]{TYPE_XLS, TYPE_XLSX});
                return true;
            case R.id.import_db:
                importDbLauncher.launch(new String[]{
                        "application/vnd.sqlite3",
                        "application/octet-stream"
                });
                return true;
            case R.id.settings:
                Intent intent = new Intent(getActivity(), AppSettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("Range")
    protected void importDb(@NonNull String importToFolder, Uri importedDbUri) {
        StorageDb storageDb = DeckDatabaseUtil
                .getInstance(getActivity().getApplicationContext())
                .getStorageDb();
        try {
            String deckName = null;
            try (Cursor cursor = getActivity().getContentResolver()
                    .query(importedDbUri, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    deckName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    deckName = storageDb.findFreeName(importToFolder, deckName);
                }
            }
            InputStream in = getActivity().getContentResolver().openInputStream(importedDbUri);
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
            adapter.refreshItems();
        } catch (Exception e) {
            e.printStackTrace();
            ExceptionDialog eDialog = new ExceptionDialog(e);
            eDialog.show(getActivity().getSupportFragmentManager(), "ImportDbDeck");
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Gets
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected FragmentListDecksBinding getBinding() {
        return binding;
    }

    public DeckRecyclerViewAdapter getAdapter() {
        return adapter;
    }

    public void refreshItems() {
        adapter.refreshItems();
    }
}