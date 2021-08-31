package pl.softfly.flashcards.ui.deck;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.AppDatabase;
import pl.softfly.flashcards.R;

public class CreateDeckDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DeckListActivity activity = (DeckListActivity) getActivity();
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_deck, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Create a new deck of cards")
                .setMessage("Pleaser enter the name.")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    EditText deckNameEditText = view.findViewById(R.id.deckName);
                    String dbName = deckNameEditText.getText().toString();
                    if (!dbName.isEmpty()) {
                        if (!dbName.endsWith(".db")) {
                            dbName += ".db";
                        } else if (dbName.toLowerCase().endsWith(".db")) {
                            dbName = dbName.substring(0, dbName.length() - 3) + ".db";
                        }

                        AppDatabase room = Room.databaseBuilder(activity.getBaseContext(),
                                AppDatabase.class, Environment.getExternalStorageDirectory().getAbsolutePath() + "/flashcards/" + dbName).build();

                        room.cardDao().deleteAll()
                                .subscribeOn(Schedulers.io())
                                .doOnComplete(() -> {
                                    room.close();
                                    activity.runOnUiThread(() -> {
                                        activity.loadDecks();
                                        Toast.makeText(activity, deckNameEditText.getText().toString() + " deck created.", Toast.LENGTH_SHORT).show();
                                    });
                                })
                                .subscribe();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .create();
    }
}
