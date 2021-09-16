package pl.softfly.flashcards.ui.card;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NoStudyCardsDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("No cards scheduled to be repeated.")
                .setPositiveButton("Back", (dialog, which) -> getActivity().finish())
                .create();
    }
}
