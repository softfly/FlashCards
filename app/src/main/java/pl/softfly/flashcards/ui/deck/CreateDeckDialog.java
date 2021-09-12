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
import pl.softfly.flashcards.db.DeckDatabase;

public class CreateDeckDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ListDecksActivity activity = (ListDecksActivity) getActivity();
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_deck, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Create a new deck of cards")
                .setMessage("Pleaser enter the name.")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    EditText deckNameEditText = view.findViewById(R.id.deckName);
                    String deckName = deckNameEditText.getText().toString();
                    if (!deckName.isEmpty()) {
                        DeckDatabase room = AppDatabaseUtil.getInstance().getDeckDatabase(getContext(), deckName);

                        room.cardDao().deleteAll()
                                .subscribeOn(Schedulers.io())
                                .doOnComplete(() -> activity.runOnUiThread(() -> {
                                    activity.loadDecks();
                                    Toast.makeText(activity, deckName + " deck created.", Toast.LENGTH_SHORT).show();
                                }))
                                .subscribe();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .create();
    }
}
