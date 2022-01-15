package pl.softfly.flashcards.filesync.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import pl.softfly.flashcards.filesync.entity.FileSynced;

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
