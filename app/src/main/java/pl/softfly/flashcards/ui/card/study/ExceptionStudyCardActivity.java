package pl.softfly.flashcards.ui.card.study;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.entity.deck.Card;

/**
 * @author Grzegorz Ziemski
 */
/*
 * I considered making classes for catching exceptions more general and making Composition,
 * but in the Manifest defined classes must inherit android.app.Activity.
 */
public class ExceptionStudyCardActivity extends DraggableStudyCardActivity {

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getExceptionHandler().tryRun(
                () -> super.onCreate(savedInstanceState),
                getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                (dialog, which) -> onBackPressed() //TODO show error
        );
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
                "Error while resuming activity."
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

    /* -----------------------------------------------------------------------------------------
     * StudyCardActivity methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void onCardChanged(Card card) {
        getExceptionHandler().tryRun(
                () -> super.onCardChanged(card),
                getSupportFragmentManager(),
                this.getClass().getName(),
                "Error while the updating the model card."
        );
    }
}
