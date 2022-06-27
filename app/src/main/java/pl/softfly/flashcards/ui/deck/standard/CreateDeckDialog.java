package pl.softfly.flashcards.ui.deck.standard;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.ui.main.MainActivity;

public class CreateDeckDialog extends DialogFragment {

    private final File currentFolder;

    private final MainActivity activity;

    public CreateDeckDialog(File currentFolder, MainActivity activity) {
        this.activity = activity;
        this.currentFolder = currentFolder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_deck, null);
        return new AlertDialog.Builder(getActivity())
                .setTitle("Create a new deck")
                .setMessage("Please enter the name:")
                .setView(view)
                .setPositiveButton("OK",
                        (dialog, which) -> getExceptionHandler().tryRun(() -> {
                            EditText deckNameEditText = view.findViewById(R.id.deckName);
                            String deckName = deckNameEditText.getText().toString();
                            DeckDatabase deckDb = createDatabase(currentFolder.getPath() + "/" + deckName);

                            // This is used to force the creation of a DB.
                            deckDb.cardDaoAsync().deleteAll()
                                    .subscribeOn(Schedulers.io())
                                    .doOnComplete(() -> onComplete(deckName))
                                    .subscribe(() -> {}, this::onError);
                        }, this::onError))
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .create();
    }

    protected void onComplete(String deckName) {
        activity.runOnUiThread(
                () -> {
                    activity.getListAllDecksFragment().getAdapter().refreshItems();
                    Toast.makeText(
                            activity,
                            "\"" + deckName + "\" deck created.",
                            Toast.LENGTH_SHORT
                    ).show();
                }, this::onError);
    }

    @NonNull
    protected void onError(Throwable e) {
        getExceptionHandler().handleException(
                e, activity.getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while creating new deck."
        );
    }

    protected DeckDatabase createDatabase(String dbPath) {
        return DeckDatabaseUtil
                .getInstance(getContext())
                .createDatabase(dbPath);
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
