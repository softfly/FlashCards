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
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.ui.main.MainActivity;
import pl.softfly.flashcards.ui.deck.standard.ListDecksFragment;

/**
 * @author Grzegorz Ziemski
 */
public class ListFoldersDecksFragment extends ListDecksFragment {

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandleOnBackPressed();
        ((MainActivity) requireActivity()).hideBackArrow();
        initCutDeckPathObserver();
    }

    @Override
    protected FolderDeckViewAdapter onCreateAdapter() {
        return new FolderDeckViewAdapter((MainActivity) getActivity());
    }

    protected void initHandleOnBackPressed() {
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        getAdapter().goFolderUp();
                    }
                });
    }

    protected void initCutDeckPathObserver() {
        getAdapter().getCutPathLiveData().observe(this, cutPath ->
                getExceptionHandler().tryRun(() -> {
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
                }, this::onErrorCutDeckPath));
    }

    protected void onErrorCutDeckPath(Throwable e) {
        getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while pasting folder / deck."
        );
    }

    /* -----------------------------------------------------------------------------------------
     * Fragment methods overridden
     * ----------------------------------------------------------------------------------------- */

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

    /* -----------------------------------------------------------------------------------------
     * Actions
     * ----------------------------------------------------------------------------------------- */

    protected void onClickPaste(String cutPath) {
        try {
            if (Files.isDirectory(Paths.get(cutPath))) {
                onClickPasteFolder(cutPath);
            } else {
                onCLickPasteDeck(cutPath);
            }
        } catch (IOException e) {
            onErrorCutDeckPath(e);
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
                }, this::onErrorCutDeckPath);
    }

    protected void onCLickPasteDeck(String cutPath) {
        getDeckDatabaseUtil().moveDatabase(cutPath, getAdapter().getCurrentFolder().getPath())
                .subscribe(() -> {
                    getAdapter().refreshItems();
                    getAdapter().getCutPathLiveData().postValue(null);
                }, this::onErrorCutDeckPath);
    }

    public boolean onSupportNavigateUp() {
        getAdapter().goFolderUp();
        return true;
    }

    /* -----------------------------------------------------------------------------------------
     * Gets
     * ----------------------------------------------------------------------------------------- */

    @Override
    public FolderDeckViewAdapter getAdapter() {
        return (FolderDeckViewAdapter) super.getAdapter();
    }


}