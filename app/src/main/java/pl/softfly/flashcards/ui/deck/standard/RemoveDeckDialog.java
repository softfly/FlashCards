package pl.softfly.flashcards.ui.deck.standard;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import io.reactivex.rxjava3.core.Completable;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.DeckDatabaseUtil;

public class RemoveDeckDialog extends DialogFragment {

    private final String path;

    private final DeckViewAdapter adapter;

    public RemoveDeckDialog(String path, DeckViewAdapter adapter) {
        this.adapter = adapter;
        this.path = path;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Remove a deck of cards")
                .setMessage("Are you sure you want to delete the deck with all cards?")
                .setPositiveButton("Yes", (dialog, which) ->
                        getExceptionHandler().tryRun(() ->
                                removeDatabase(path)
                                        .subscribe(() -> {
                                            adapter.refreshItems();
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getContext(),
                                                            "The deck has been removed.",
                                                            Toast.LENGTH_SHORT
                                                    ).show());
                                        }), this::onError))
                .setNegativeButton("No", (dialog, which) -> {})
                .create();
    }

    @NonNull
    protected void onError(Throwable e) {
        getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while removing deck."
        );
    }

    protected Completable removeDatabase(String dbPath) {
        return DeckDatabaseUtil
                .getInstance(getContext())
                .removeDatabase(dbPath);
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
