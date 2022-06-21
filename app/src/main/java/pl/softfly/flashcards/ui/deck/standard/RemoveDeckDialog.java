package pl.softfly.flashcards.ui.deck.standard;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import io.reactivex.rxjava3.functions.Consumer;
import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.ui.deck.folder.CreateFolderDialog;

public class RemoveDeckDialog extends DialogFragment {

    private final String path;

    private final DeckRecyclerViewAdapter adapter;

    public RemoveDeckDialog(String path, DeckRecyclerViewAdapter adapter) {
        this.adapter = adapter;
        this.path = path;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Context context = activity.getApplicationContext();
        return new AlertDialog.Builder(getActivity())
                .setTitle("Remove a deck of cards")
                .setMessage("Are you sure you want to delete the deck with all cards?")
                .setPositiveButton("Yes", (dialog, which) ->
                        getExceptionHandler().tryHandleException(() ->
                                DeckDatabaseUtil
                                        .getInstance(getContext())
                                        .removeDatabase(path)
                                        .subscribe(() -> {
                                            adapter.refreshItems();
                                            getActivity().runOnUiThread(() ->
                                                    Toast.makeText(
                                                            getContext(),
                                                            "The deck has been removed.",
                                                            Toast.LENGTH_SHORT
                                                    ).show());
                                        }), getOnError()))
                .setNegativeButton("No", (dialog, which) -> {
                })
                .create();
    }

    @NonNull
    protected Consumer<? super Throwable> getOnError() {
        return e -> getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                CreateFolderDialog.class.getSimpleName(),
                "Error while removing deck."
        );
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}