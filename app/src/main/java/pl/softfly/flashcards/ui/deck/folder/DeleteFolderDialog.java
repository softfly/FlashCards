package pl.softfly.flashcards.ui.deck.folder;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.ui.deck.standard.DeckRecyclerViewAdapter;

public class DeleteFolderDialog extends DialogFragment {

    private final File currentFolder;

    private final DeckRecyclerViewAdapter adapter;

    public DeleteFolderDialog(File currentFolder, DeckRecyclerViewAdapter adapter) {
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
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(
                                                requireContext(),
                                                "The folder has been removed.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });
                                });
                    } catch (Exception e) {
                        getExceptionHandler().handleException(
                                e, getActivity().getSupportFragmentManager(),
                                DeleteFolderDialog.class.getSimpleName(),
                                "Error while deleting folder."
                        );
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

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }

    @Nullable
    protected AppDatabase getAppDatabase() {
        return AppDatabaseUtil
                .getInstance(getContext())
                .getDatabase();
    }
}
