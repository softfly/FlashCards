package pl.softfly.flashcards.ui.deck.standard;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import pl.softfly.flashcards.Config;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.FragmentListDecksBinding;
import pl.softfly.flashcards.ui.ExportImportDbUtil;
import pl.softfly.flashcards.ui.IconWithTextInTopbarFragment;
import pl.softfly.flashcards.ui.MainActivity;
import pl.softfly.flashcards.ui.app.settings.AppSettingsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ListDecksFragment extends IconWithTextInTopbarFragment {

    private FragmentListDecksBinding binding;

    private DeckRecyclerViewAdapter adapter;

    protected ExportImportDbUtil exportImportDbUtil = new ExportImportDbUtil(this);

    /* -----------------------------------------------------------------------------------------
     * Fragment methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = onCreateAdapter();
        askPermissionManageExternalStorage();
        setHasOptionsMenu(true);
    }

    protected DeckRecyclerViewAdapter onCreateAdapter() {
        return new DeckRecyclerViewAdapter((MainActivity) getActivity(), this);
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
                ((MainActivity)getActivity()).getFileSyncUtil().launchImportExcel(adapter.getCurrentFolder().getPath());
                return true;
            case R.id.import_db:
                exportImportDbUtil.launchImportDb(getAdapter().getCurrentFolder().getPath());
                return true;
            case R.id.settings:
                Intent intent = new Intent(getActivity(), AppSettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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