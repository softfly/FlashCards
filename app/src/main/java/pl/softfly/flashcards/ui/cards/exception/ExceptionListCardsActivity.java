package pl.softfly.flashcards.ui.cards.exception;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import pl.softfly.flashcards.ui.cards.file_sync.FileSyncListCardsActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionListCardsActivity extends FileSyncListCardsActivity {

    private ExceptionCardRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        exceptionHandler.tryHandleException(
                () -> super.onCreate(savedInstanceState),
                getSupportFragmentManager(),
                ExceptionListCardsActivity.class.getName(),
                (dialog, which) -> onBackPressed()
        );
    }

    @Override
    protected ExceptionCardRecyclerViewAdapter onCreateRecyclerViewAdapter() {
        adapter = new ExceptionCardRecyclerViewAdapter(this, getDeckName());
        setAdapter(adapter);
        return adapter;
    }

    @NonNull
    @Override
    protected ItemTouchHelper.Callback onCreateTouchHelper() {
        return new ExceptionCardTouchHelper(adapter);
    }

    @Override
    public void onRestart() {
        exceptionHandler.tryHandleException(
                () -> super.onRestart(),
                getSupportFragmentManager(),
                ExceptionListCardsActivity.class.getSimpleName() + "_OnRestart"
        );
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        try {
            return super.onCreateOptionsMenu(menu);
        } catch (Exception e) {
            exceptionHandler.handleException(
                    e, getSupportFragmentManager(),
                    ExceptionListCardsActivity.class.getSimpleName() + "_OnCreateOptionsMenu",
                    (dialog, which) -> onBackPressed()
            );
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            exceptionHandler.handleException(
                    e, getSupportFragmentManager(),
                    ExceptionListCardsActivity.class.getSimpleName() + "_OnOptionsItemSelected"
            );
            return false;
        }
    }

    protected void setAdapter(ExceptionCardRecyclerViewAdapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
    }
}