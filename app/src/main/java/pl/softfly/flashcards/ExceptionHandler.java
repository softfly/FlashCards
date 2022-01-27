package pl.softfly.flashcards;

import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import pl.softfly.flashcards.ui.ExceptionDialog;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionHandler {

    private FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            String tag
    ) {
        tryHandleException(r, manager, tag, null, null);
    }

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            String tag,
            String message
    ) {
        tryHandleException(r, manager, tag, message, null);
    }

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            String tag,
            DialogInterface.OnClickListener callback
    ) {
        tryHandleException(r, manager, tag, null, callback);
    }

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            String tag,
            String message,
            DialogInterface.OnClickListener callback
    ) {
        try {
            r.run();
        } catch (Exception e) {
            handleException(e, manager, tag, message, callback);
        }
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull FragmentManager manager,
            String tag
    ) {
        handleException(e, manager, tag, null, null);
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull FragmentManager manager,
            String tag,
            DialogInterface.OnClickListener positiveListener
    ) {
        handleException(e, manager, tag, null, positiveListener);
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull FragmentManager manager,
            String tag,
            String message,
            DialogInterface.OnClickListener positiveListener
    ) {
        e.printStackTrace();
        crashlytics.setCustomKey("message", message);
        crashlytics.setCustomKey("tag", tag);
        crashlytics.recordException(e);
        ExceptionDialog dialog = new ExceptionDialog(e, message, positiveListener);
        dialog.show(manager, tag);
    }
}