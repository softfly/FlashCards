package pl.softfly.flashcards.ui.deck.folder;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

import io.reactivex.rxjava3.functions.Consumer;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

public class CreateFolderDialog extends DialogFragment {

    private final File currentFolder;

    private final DeckRecyclerViewAdapter adapter;

    public CreateFolderDialog(File currentFolder, DeckRecyclerViewAdapter adapter) {
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
                        (dialog, which) -> getExceptionHandler().tryHandleException(() -> {
                            EditText deckNameEditText = view.findViewById(R.id.folderName);
                            String newFolderName = deckNameEditText.getText().toString();
                            File folder = new File(currentFolder.getPath() + "/" + newFolderName);
                            if (!folder.exists()) {
                                //noinspection ResultOfMethodCallIgnored
                                folder.mkdir();
                                adapter.refreshItems();
                                Toast.makeText(
                                        requireContext(),
                                        "\"" + newFolderName + "\" folder created.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            } else {
                                Toast.makeText(
                                        requireContext(),
                                        "\"" + newFolderName + "\" folder already exists.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }, getOnError()))
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .create();
    }

    @NonNull
    protected Consumer<? super Throwable> getOnError() {
        return e -> getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                CreateFolderDialog.class.getSimpleName(),
                "Error while creating new folder."
        );
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
