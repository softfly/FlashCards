package pl.softfly.flashcards.ui.deck.folder;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ui.base.BaseDialogFragment;
import pl.softfly.flashcards.ui.main.MainActivity;
import pl.softfly.flashcards.ui.deck.standard.DeckViewAdapter;

public class DeleteFolderDialog extends BaseDialogFragment {

    private final File currentFolder;

    private final DeckViewAdapter adapter;

    public DeleteFolderDialog(File currentFolder, DeckViewAdapter adapter) {
        this.adapter = adapter;
        this.currentFolder = currentFolder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Remove a deck of cards")
                .setMessage("Are you sure you want to delete the folder with all decks?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        removeFolderWithAllFiles(currentFolder);

                        getAppDatabase().deckDaoAsync()
                                .deleteByStartWithPath(currentFolder.getPath())
                                .subscribeOn(Schedulers.io())
                                .subscribe(() -> {
                                    adapter.refreshItems();
                                    ((MainActivity) getActivity()).runOnUiThread(
                                            () -> Toast.makeText(
                                                    requireContext(),
                                                    "The folder has been removed.",
                                                    Toast.LENGTH_SHORT
                                            ).show(), this::onError);
                                });
                    } catch (IOException e) {
                        onError(e);
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                })
                .create();
    }

    private void removeFolderWithAllFiles(@NonNull File folder) throws IOException {
        Files.walk(Paths.get(folder.getPath()))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @NonNull
    protected void onError(Throwable e) {
        getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while deleting folder."
        );
    }
}
