package pl.softfly.flashcards;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import io.reactivex.rxjava3.functions.Consumer;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.ui.ExceptionDialog;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionHandler {

    private static ExceptionHandler INSTANCE;

    @NonNull
    private FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

    public static synchronized ExceptionHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ExceptionHandler();
        }
        return INSTANCE;
    }

    private ExceptionHandler() { }

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag
    ) {
        tryHandleException(r, manager, tag, null, null);
    }

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            @NonNull String message
    ) {
        tryHandleException(r, manager, tag, message, null);
    }

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            DialogInterface.OnClickListener callback
    ) {
        tryHandleException(r, manager, tag, null, callback);
    }

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            @NonNull String message,
            DialogInterface.OnClickListener callback
    ) {
        try {
            r.run();
        } catch (Exception e) {
            handleException(e, manager, tag, message, callback);
        }
    }

    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull Consumer<? super Throwable> onError
    ) {
        try {
            r.run();
        } catch (Exception e) {
            try {
                onError.accept(e);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull FragmentManager manager,
            @NonNull String tag
    ) {
        handleException(e, manager, tag, null, null);
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            @NonNull String message
    ) {
        handleException(e, manager, tag, message, null);
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            DialogInterface.OnClickListener positiveListener
    ) {
        handleException(e, manager, tag, null, positiveListener);
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            @NonNull String message,
            DialogInterface.OnClickListener positiveListener
    ) {
        e.printStackTrace();
        if (Config.getInstance().isCrashlyticsEnabled()) {
            crashlytics.setCustomKey("message", message);
            crashlytics.setCustomKey("tag", tag);
            crashlytics.recordException(e);
        }
        ExceptionDialog dialog = new ExceptionDialog(e, message, positiveListener);
        dialog.show(manager, tag);
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull AppCompatActivity activity,
            @NonNull String tag
    ) {
        handleException(e, activity, tag, null, null);
    }

    public void handleException(
            @NonNull Throwable e,
            @NonNull AppCompatActivity activity,
            @NonNull String tag,
            @NonNull String message,
            DialogInterface.OnClickListener positiveListener
    ) {
        e.printStackTrace();
        if (Config.getInstance().isCrashlyticsEnabled()) {
            crashlytics.setCustomKey("message", message);
            crashlytics.setCustomKey("tag", tag);
            crashlytics.recordException(e);
        }

        if (activity.getLifecycle()
                .getCurrentState()
                .isAtLeast(Lifecycle.State.RESUMED)) {
            ExceptionDialog dialog = new ExceptionDialog(e, message, positiveListener);
            dialog.show(activity.getSupportFragmentManager(), tag);
        }
    }
}