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

public class SetUpAutoSyncFileDialog extends DialogFragment {

    protected FileSynced fileSynced;

    protected AppCompatActivity activity;

    protected Runnable andThen;

    /**
     * 3. Set up the file to auto-sync in the future.
     */
    protected DialogInterface.OnClickListener positiveButton = (dialog, which) -> {
        fileSynced.setAutoSync(true);
        Toast.makeText(
                activity.getApplicationContext(),
                "Auto-sync has been set up.",
                Toast.LENGTH_SHORT
        ).show();
        andThen.run();
    };

    protected DialogInterface.OnClickListener negativeButton = (dialog, which) -> andThen.run();

    public SetUpAutoSyncFileDialog(
            FileSynced fileSynced,
            AppCompatActivity activity,
            Runnable andThen
    ) {
        this.fileSynced = fileSynced;
        this.activity = activity;
        this.andThen = andThen;
    }

    /**
     * 2. Ask if the deck should auto-sync with this file.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireActivity())
                .setTitle("Do you want to enable auto-sync with this file?")
                .setMessage("When you open the deck or change cards in the deck, your file will be synced.")
                .setPositiveButton("Yes", positiveButton)
                .setNegativeButton("No", negativeButton)
                .create();
    }
}
