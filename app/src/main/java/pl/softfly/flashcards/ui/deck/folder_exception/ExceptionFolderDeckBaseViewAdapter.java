package pl.softfly.flashcards.ui.deck.folder_exception;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.ui.main.MainActivity;
import pl.softfly.flashcards.ui.deck.folder.FolderDeckBaseViewAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionFolderDeckBaseViewAdapter extends FolderDeckBaseViewAdapter {

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    public ExceptionFolderDeckBaseViewAdapter(@NonNull MainActivity activity) {
        super(activity);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateFolderViewHolder(View view) {
        return new ExceptionFolderDeckViewHolder(view, this);
    }

    protected RecyclerView.ViewHolder onCreateDeckViewHolder(View view) {
        return new ExceptionFolderViewHolder(view, this);
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
