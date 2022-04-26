package pl.softfly.flashcards.ui.deck.folder;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import pl.softfly.flashcards.ui.ExceptionDialog;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

public class DeleteFolderDialog extends DialogFragment {

    private File currentFolder;

    public DeleteFolderDialog(File currentFolder) {
        this.currentFolder = currentFolder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ListDecksActivity activity = (ListDecksActivity) getActivity();

        return new AlertDialog.Builder(getActivity())
                .setTitle("Remove a deck of cards")
                .setMessage("Are you sure you want to delete the folder with all decks?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        removeFolderWithAllFiles(currentFolder);
                        activity.loadDecks();
                        Toast.makeText(
                                getContext(),
                                "The folder has been removed.",
                                Toast.LENGTH_SHORT
                        ).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        ExceptionDialog exceptionDialog = new ExceptionDialog(e);
                        exceptionDialog.show(
                                activity.getSupportFragmentManager(),
                                DeleteFolderDialog.class.getSimpleName()
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
}
