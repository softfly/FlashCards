package pl.softfly.flashcards.ui.deck.folder;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.MainActivity;
import pl.softfly.flashcards.ui.deck.ListDecksFragment;

/**
 * @author Grzegorz Ziemski
 */
public class ListFoldersDecksFragment extends ListDecksFragment {

    private FolderDeckRecyclerViewAdapter adapter;

    public ListFoldersDecksFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getAdapter().goFolderUp();
            }
        });
        ((MainActivity) requireActivity()).hideBackArrow();
    }

    @Override
    protected FolderDeckRecyclerViewAdapter onCreateAdapter() {
        adapter = new FolderDeckRecyclerViewAdapter((MainActivity) getActivity());
        return adapter;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_folder: {
                DialogFragment dialog = new CreateFolderDialog(getAdapter().getCurrentFolder(), getAdapter());
                dialog.show(getActivity().getSupportFragmentManager(), "CreateFolder");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onSupportNavigateUp() {
        getAdapter().goFolderUp();
        return true;
    }

    /* -----------------------------------------------------------------------------------------
     * Gets
     * ----------------------------------------------------------------------------------------- */

    @Override
    public FolderDeckRecyclerViewAdapter getAdapter() {
        return adapter;
    }
}