package pl.softfly.flashcards.ui.card.study;

import android.os.Bundle;

import pl.softfly.flashcards.entity.deck.Card;

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

    protected void onChanged(Card card) {
        getExceptionHandler().tryHandleException(
                () -> super.onChanged(card),
                getSupportFragmentManager(),
                ExceptionStudyCardActivity.class.getName(),
                "Error while the updating the model card."
        );
    }

}
