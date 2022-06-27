package pl.softfly.flashcards.ui.deck.recent_exception;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.ui.deck.recent.RecentDeckBaseViewAdapter;
import pl.softfly.flashcards.ui.main.MainActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionRecentDeckBaseViewAdapter extends RecentDeckBaseViewAdapter {

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    public ExceptionRecentDeckBaseViewAdapter(@NonNull MainActivity activity) {
        super(activity);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateDeckViewHolder(View view) {
        return new ExceptionRecentDeckViewHolder(view, this);
    }

    /* -----------------------------------------------------------------------------------------
     * Methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        getExceptionHandler().tryRun(
                () -> super.onBindViewHolder(holder, position),
                getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_OnBindViewHolder"
        );
    }

}
