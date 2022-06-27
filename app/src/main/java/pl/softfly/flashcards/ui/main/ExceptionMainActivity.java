package pl.softfly.flashcards.ui.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

/**
 * @author Grzegorz Ziemski
 */
/*
 * I considered making classes for catching exceptions more general and making Composition,
 * but in the Manifest defined classes must inherit android.app.Activity.
 */
public class ExceptionMainActivity extends MainActivity {

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExceptionHandler().setDefaultUncaughtExceptionHandler(this);
    }

    /* -----------------------------------------------------------------------------------------
     * Activity methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void onResume() {
        getExceptionHandler().tryRun(
                super::onResume,
                getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while resuming main activity."
        );
    }

    @Override
    public void onRestart() {
        getExceptionHandler().tryRun(
                super::onRestart,
                getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while restarting activity."
        );
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        try {
            return super.onCreateOptionsMenu(menu);
        } catch (Exception e) {
            getExceptionHandler().handleException(
                    e, getSupportFragmentManager(),
                    this.getClass().getSimpleName(),
                    "Error while creating toolbar menu."
            );
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            getExceptionHandler().handleException(
                    e, getSupportFragmentManager(),
                    this.getClass().getSimpleName(),
                    "Error while selecting item in the toolbar menu."
            );
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        try {
            return super.onSupportNavigateUp();
        } catch (Exception e) {
            getExceptionHandler().handleException(
                    e, getSupportFragmentManager(),
                    this.getClass().getSimpleName(),
                    "Error when pressing back."
            );
            return false;
        }
    }
}