package pl.softfly.flashcards;

import android.content.DialogInterface;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import io.reactivex.rxjava3.functions.Consumer;
import pl.softfly.flashcards.ui.ExceptionDialog;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionHandler {

    private static ExceptionHandler INSTANCE;

    @NonNull
    private final FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

    private ExceptionHandler() {
    }

    public static synchronized ExceptionHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ExceptionHandler();
        }
        return INSTANCE;
    }

    public void tryRun(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag
    ) {
        tryHandleException(r, manager, tag, null, null);
    }

    @Deprecated
    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag
    ) {
        tryHandleException(r, manager, tag, null, null);
    }

    public void tryRun(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            @NonNull String message
    ) {
        tryHandleException(r, manager, tag, message, null);
    }

    @Deprecated
    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            @NonNull String message
    ) {
        tryHandleException(r, manager, tag, message, null);
    }

    public void tryRun(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            DialogInterface.OnClickListener callback
    ) {
        tryHandleException(r, manager, tag, null, callback);
    }

    @Deprecated
    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull FragmentManager manager,
            @NonNull String tag,
            DialogInterface.OnClickListener callback
    ) {
        tryHandleException(r, manager, tag, null, callback);
    }

    public void tryRun(
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

    @Deprecated
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

    public void tryRun(
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

    @Deprecated
    public void tryHandleException(
            @NonNull Runnable r,
            @NonNull Consumer<? super Throwable> onError
    ) {
        tryRun(r, onError);
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

    public void setDefaultUncaughtExceptionHandler(AppCompatActivity activity) {
        Thread.setDefaultUncaughtExceptionHandler((paramThread, paramThrowable) -> {
            new Thread(() -> {
                Looper.prepare();
                Toast.makeText(activity, "Fatal error", Toast.LENGTH_LONG).show();
                Looper.loop();
            }).start();
            try {
                Thread.sleep(500); // Let the Toast display before app will get shutdown
            } catch (InterruptedException e) {
            }
            System.exit(2);
        });
    }
}