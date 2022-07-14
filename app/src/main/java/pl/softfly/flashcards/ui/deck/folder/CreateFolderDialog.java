package pl.softfly.flashcards.ui.deck.folder;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.base.BaseDialogFragment;
import pl.softfly.flashcards.ui.deck.standard.DeckViewAdapter;

public class CreateFolderDialog extends BaseDialogFragment {

    private final File currentFolder;

    private final DeckViewAdapter adapter;

    public CreateFolderDialog(File currentFolder, DeckViewAdapter adapter) {
        this.adapter = adapter;
        this.currentFolder = currentFolder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_folder, null);
        return new AlertDialog.Builder(getActivity())
                .setTitle("Create a new folder")
                .setMessage("Please enter the name:")
                .setView(view)
                .setPositiveButton("OK",
                        (dialog, which) -> getExceptionHandler().tryRun(() -> {
                            EditText deckNameEditText = view.findViewById(R.id.folderName);
                            String newFolderName = deckNameEditText.getText().toString();
                            File folder = new File(currentFolder.getPath() + "/" + newFolderName);
                            if (!folder.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                folder.mkdir();
                                adapter.refreshItems();
                                showToastFolderCreated(newFolderName);
                            } else {
                                showToastFolderExists(newFolderName);
                            }
                        }, this::onError))
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .create();
    }

    protected void showToastFolderCreated(String newFolderName) {
        Toast.makeText(
                requireContext(),
                "\"" + newFolderName + "\" folder created.",
                Toast.LENGTH_SHORT
        ).show();
    }

    protected void showToastFolderExists(String newFolderName) {
        Toast.makeText(
                requireContext(),
                "\"" + newFolderName + "\" folder already exists.",
                Toast.LENGTH_SHORT
        ).show();
    }

    @NonNull
    protected void onError(Throwable e) {
        getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while creating new folder."
        );
    }
}
