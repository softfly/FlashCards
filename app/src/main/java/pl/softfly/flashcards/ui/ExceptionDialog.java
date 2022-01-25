package pl.softfly.flashcards.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionDialog extends DialogFragment {

    private final Throwable e;

    private String message;

    private AlertDialog alertDialog;

    public ExceptionDialog(Throwable e) {
        this.e = e;
    }

    public ExceptionDialog(String message, Throwable e) {
        this.message = message;
        this.e = e;
    }

    public static void showExceptionDialog(@NonNull Throwable e, @NonNull FragmentManager manager, String tag) {
        e.printStackTrace();
        ExceptionDialog dialog = new ExceptionDialog(e);
        dialog.show(manager, tag);
    }

    public static void showExceptionDialog(String message, @NonNull Throwable e, @NonNull FragmentManager manager, String tag) {
        e.printStackTrace();
        ExceptionDialog dialog = new ExceptionDialog(message, e);
        dialog.show(manager, tag);
    }

    public static void showExceptionDialog(@NonNull Runnable r, String message, @NonNull FragmentManager manager, String tag) {
        try {
            r.run();
        } catch (Exception e) {
            showExceptionDialog(message, e, manager, tag);
        }
    }

    public static void showExceptionDialog(@NonNull Runnable r, @NonNull FragmentManager manager, String tag) {
        try {
            r.run();
        } catch (Exception e) {
            showExceptionDialog(e, manager, tag);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage(nonEmpty(message) ? message : e.getMessage())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {})
                .setNegativeButton("Show Exception", (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();
        alertDialog.setOnShowListener(dialog -> {
            Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(this::showException);
        });
        return alertDialog;
    }

    protected void showException(@NonNull View view) {
        TextView messageTextView = (TextView) alertDialog.findViewById(android.R.id.message);
        messageTextView.setText(ExceptionUtils.getStackTrace(e));
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setText("Hide Exception");
        negativeButton.setOnClickListener(this::hideException);
    }

    protected void hideException(View view) {
        TextView messageTextView = (TextView) alertDialog.findViewById(android.R.id.message);
        messageTextView.setText(nonEmpty(message) ? message : e.getMessage());
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setText("Show Exception");
        negativeButton.setOnClickListener(this::showException);
    }

    protected boolean nonEmpty(@Nullable String str) {
        return str != null && !str.isEmpty();
    }
}
