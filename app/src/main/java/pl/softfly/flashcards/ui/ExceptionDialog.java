package pl.softfly.flashcards.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ExceptionDialog extends DialogFragment {

    private Exception e;

    public ExceptionDialog(Exception e) {
        this.e = e;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Exception")
                .setMessage(e.getMessage())
                .setNeutralButton(android.R.string.ok, (dialog, which) -> getActivity().finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();
    }
}
