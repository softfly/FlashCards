package pl.softfly.flashcards.ui.deck.folder;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.ui.MainActivity;
import pl.softfly.flashcards.ui.deck.standard.ListDecksFragment;

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
        initCutDeckPathObserver();
    }

    protected void initCutDeckPathObserver() {
        getAdapter().getCutPathLiveData().observe(this, cutPath -> {
            if (cutPath != null) {
                if (Files.isDirectory(Paths.get(cutPath))) {
                    getBinding().pasteButton.setText("Paste the folder here");
                } else {
                    getBinding().pasteButton.setText("Paste the deck here");
                }
                getBinding().pasteMenuBottom.setVisibility(View.VISIBLE);
                getBinding().cancelButton.setOnClickListener(v -> getAdapter().getCutPathLiveData().postValue(null));
                getBinding().pasteButton.setOnClickListener(v -> onClickPaste(cutPath));
            } else {
                getBinding().pasteMenuBottom.setVisibility(View.GONE);
            }
        });
    }

    protected void onClickPaste(String cutPath) {
        try {
            if (Files.isDirectory(Paths.get(cutPath))) {
                onClickPasteFolder(cutPath);
            } else {
                DeckDatabaseUtil.getInstance(getContext())
                        .moveDatabase(cutPath, getAdapter().getCurrentFolder().getPath())
                        .subscribe(() -> {
                            getAdapter().refreshItems();
                            getAdapter().getCutPathLiveData().postValue(null);
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
            getExceptionHandler().handleException(
                    e, getParentFragmentManager(),
                    ListFoldersDecksFragment.class.getSimpleName(),
                    "Error while paste files."
            );
        }
    }

    protected void onClickPasteFolder(String cutPath) throws IOException {
        DeckDatabaseUtil deckDatabaseUtil = DeckDatabaseUtil
                .getInstance(getContext());

        File from = new File(cutPath);
        String replacementFolderPath = getAdapter().getCurrentFolder().getPath() + "/" + from.getName();
        (new File(replacementFolderPath)).mkdir();

        Flowable.just(Files.walk(Paths.get(cutPath))
                        .filter(Files::isRegularFile)
                        .map(Path::toString)
                        .collect(Collectors.toList()))
                .flatMap(list -> Flowable.fromIterable(list))
                .flatMapCompletable(
                        dbPath -> deckDatabaseUtil.moveDatabase(dbPath, cutPath, replacementFolderPath)
                )
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    from.delete();
                    getAdapter().refreshItems();
                    getAdapter().getCutPathLiveData().postValue(null);
                });
    }

    @Override
    protected FolderDeckRecyclerViewAdapter onCreateAdapter() {
        adapter = new FolderDeckRecyclerViewAdapter((MainActivity) getActivity(), this);
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

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}