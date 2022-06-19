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

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.ui.MainActivity;

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
                        (dialog, which) -> getExceptionHandler().tryHandleException(() -> {
                            EditText deckNameEditText = view.findViewById(R.id.deckName);
                            String deckName = deckNameEditText.getText().toString();
                            DeckDatabase deckDb = DeckDatabaseUtil
                                    .getInstance(getContext())
                                    .createDatabase(currentFolder.getPath() + "/" + deckName);

                            // This is used to force the creation of a DB.
                            deckDb.cardDaoAsync().deleteAll()
                                    .subscribeOn(Schedulers.io())
                                    .doOnComplete(() -> activity.runOnUiThread(
                                                    () -> getExceptionHandler().tryHandleException(() -> {
                                                        activity.getListAllDecksFragment().getAdapter().refreshItems();
                                                        Toast.makeText(
                                                                activity,
                                                                "\"" + deckName + "\" deck created.",
                                                                Toast.LENGTH_SHORT
                                                        ).show();
                                                    }, getOnError())
                                            )
                                    )
                                    .doOnError(getOnError())
                                    .subscribe(() -> {
                                    }, getOnError());
                        }, getOnError()))
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .create();
    }

    @NonNull
    protected Consumer<? super Throwable> getOnError() {
        return e -> getExceptionHandler().handleException(
                e, activity.getSupportFragmentManager(),
                CreateDeckDialog.class.getSimpleName(),
                "Error while creating new deck."
        );
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
