package pl.softfly.flashcards.filesync.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * 1.1 If Yes, display a message that deck editing is blocked and end use case.
 */
public class EditingDeckLockedDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireActivity())
                .setTitle("Deck editing is locked.")
                .setMessage("Wait for the edit to be unlocked. It is likely blocked by a sync, import or export process in the background.")
                .setPositiveButton("Ok", null)
                .create();
    }
}
