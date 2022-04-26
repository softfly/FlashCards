package pl.softfly.flashcards.ui.deck;

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
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.ui.card.study.StudyCardActivity;

public class CreateDeckDialog extends DialogFragment {

    private File currentFolder;

    public CreateDeckDialog(File currentFolder) {
        this.currentFolder = currentFolder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ListDecksActivity activity = (ListDecksActivity) getActivity();
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_deck, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Create a new deck")
                .setMessage("Please enter the name:")
                .setView(view)
                .setPositiveButton("OK",
                        (dialog, which) -> getExceptionHandler().tryHandleException(() -> {
                            EditText deckNameEditText = view.findViewById(R.id.deckName);
                            String deckName = deckNameEditText.getText().toString();
                            DeckDatabase deckDb = AppDatabaseUtil
                                    .getInstance(getContext())
                                    .getDeckDatabase(currentFolder.getPath() + "/" + deckName);

                            // This is used to force the creation of a DB.
                            deckDb.cardDaoAsync().deleteAll()
                                    .subscribeOn(Schedulers.io())
                                    .doOnComplete(() -> activity.runOnUiThread(() -> {
                                        activity.loadDecks();
                                        Toast.makeText(
                                                activity,
                                                "\"" + deckName + "\" deck created.",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }))
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
                e, getActivity().getSupportFragmentManager(),
                CreateDeckDialog.class.getSimpleName(),
                "Error while read the deck view settings."
        );
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
