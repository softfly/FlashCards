package pl.softfly.flashcards;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Grzegorz Ziemski
 */
public class FlashCardsApp extends Application {

    private Activity activeActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(setupActivityListener());
    }

    protected ActivityLifecycleCallbacks setupActivityListener() {
        return new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                FlashCardsApp.this.activeActivity = activity;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                FlashCardsApp.this.activeActivity = activity;
            }

            @Override
            public void onActivityResumed(Activity activity) {
                FlashCardsApp.this.activeActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                FlashCardsApp.this.activeActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                // don't clear current activity because activity may get stopped after
                // the new activity is resumed
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // don't clear current activity because activity may get destroyed after
                // the new activity is resumed
            }
        };
    }

    public AppCompatActivity getActiveActivity() {
        return (AppCompatActivity) activeActivity;
    }
}
