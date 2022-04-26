package pl.softfly.flashcards.ui.deck;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.storage.StorageDb;

public class RemoveDeckDialog extends DialogFragment {

    private final String path;

    public RemoveDeckDialog(String path) {
        this.path = path;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ListDecksActivity activity = (ListDecksActivity) getActivity();

        return new AlertDialog.Builder(getActivity())
                .setTitle("Remove a deck of cards")
                .setMessage("Are you sure you want to delete the deck with all cards?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    StorageDb storageDb = AppDatabaseUtil
                            .getInstance(activity.getApplicationContext())
                            .getStorageDb();

                    if (storageDb.removeDatabase(path)) {
                        activity.loadDecks();
                        Toast.makeText(
                                getContext(),
                                "The deck has been removed.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                })
                .create();
    }
}
