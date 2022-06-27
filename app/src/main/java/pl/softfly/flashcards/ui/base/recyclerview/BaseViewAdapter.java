package pl.softfly.flashcards.ui.base.recyclerview;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;

/**
 * @author Grzegorz Ziemski
 */
public abstract class BaseViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private final AppCompatActivity activity;

    public BaseViewAdapter(AppCompatActivity activity) {
        this.activity = activity;
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }

    @Nullable
    protected DeckDatabase getDeckDatabase(String dbPath) {
        return DeckDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getDatabase(dbPath);
    }

    public AppCompatActivity getActivity() {
        return activity;
    }
}
