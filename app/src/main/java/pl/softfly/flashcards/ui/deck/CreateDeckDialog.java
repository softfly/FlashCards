package pl.softfly.flashcards.ui.deck;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.deck.DeckDatabase;
import pl.softfly.flashcards.ui.ExceptionDialog;

public class CreateDeckDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ListDecksActivity activity = (ListDecksActivity) getActivity();
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_deck, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Create a new deck")
                .setMessage("Please enter the name:")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    EditText deckNameEditText = view.findViewById(R.id.deckName);
                    String deckName = deckNameEditText.getText().toString();
                    try {
                        DeckDatabase room = AppDatabaseUtil.getInstance(getContext()).getDeckDatabase(deckName);
                        // This is used to force the creation of a DB.
                        room.cardDaoAsync().deleteAll()
                                .subscribeOn(Schedulers.io())
                                .doOnComplete(() -> activity.runOnUiThread(() -> {
                                    activity.loadDecks();
                                    Toast.makeText(
                                            activity,
                                            "\"" + deckName + "\" deck created.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }))
                                .subscribe();
                    } catch (Exception e) {
                        e.printStackTrace();
                        ExceptionDialog eDialog = new ExceptionDialog(e);
                        eDialog.show(getActivity().getSupportFragmentManager(), this.getTag());
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .create();
    }
}
