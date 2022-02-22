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

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionDialog extends DialogFragment {

    private final Throwable e;

    private String message;

    private AlertDialog alertDialog;

    @Nullable
    private DialogInterface.OnClickListener positiveListener;

    public ExceptionDialog(Throwable e) {
        this(e, null, null);
    }

    public ExceptionDialog(Throwable e, DialogInterface.OnClickListener positiveListener) {
        this(e, null, positiveListener);
    }

    public ExceptionDialog(Throwable e, String message) {
        this(e, message, null);
    }

    public ExceptionDialog(Throwable e, String message, @Nullable DialogInterface.OnClickListener positiveListener) {
        this.e = e;
        this.message = message;
        this.positiveListener = positiveListener != null ? positiveListener : (dialog, which) -> {} ;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage(nonEmpty(message) ? message : e.getMessage())
                .setPositiveButton(android.R.string.ok, positiveListener)
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
        TextView messageTextView = alertDialog.findViewById(android.R.id.message);
        messageTextView.setText(ExceptionUtils.getStackTrace(e));
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setText("Hide Exception");
        negativeButton.setOnClickListener(this::hideException);
    }

    protected void hideException(View view) {
        TextView messageTextView = alertDialog.findViewById(android.R.id.message);
        messageTextView.setText(nonEmpty(message) ? message : e.getMessage());
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setText("Show Exception");
        negativeButton.setOnClickListener(this::showException);
    }

    protected boolean nonEmpty(@Nullable String str) {
        return str != null && !str.isEmpty();
    }
}
