package pl.softfly.flashcards.ui.card.study;

import android.os.Bundle;

import pl.softfly.flashcards.ui.cards.exception.ExceptionListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionStudyCardActivity extends DraggableStudyCardActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getExceptionHandler().tryHandleException(
                () -> super.onCreate(savedInstanceState),
                getSupportFragmentManager(),
                ExceptionStudyCardActivity.class.getName(),
                (dialog, which) -> onBackPressed()
        );
    }

}
