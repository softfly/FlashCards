package pl.softfly.flashcards.filesync.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SetUpAutoSyncFileDialog extends DialogFragment {


    private DialogInterface.OnClickListener positiveButton;

    private DialogInterface.OnClickListener negativeButton;

    public SetUpAutoSyncFileDialog(
            DialogInterface.OnClickListener positiveButton,
            DialogInterface.OnClickListener negativeButton
    ) {
        this.positiveButton = positiveButton;
        this.negativeButton = negativeButton;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Do you want to enable auto-sync with this file?")
                .setMessage("When you open the deck or change cards in the deck, your file will be synced.")
                .setPositiveButton("Yes", positiveButton)
                .setNegativeButton("No", negativeButton)
                .create();
    }
}
