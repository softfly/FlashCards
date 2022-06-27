package pl.softfly.flashcards.ui.cards.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.ui.cards.select.SelectCardTouchHelper;

/**
 * @author Grzegorz Ziemski
 */
public class ExceptionCardTouchHelper extends SelectCardTouchHelper {

    public ExceptionCardTouchHelper(ExceptionCardBaseViewAdapter adapter) {
        super(adapter);
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        getExceptionHandler().tryRun(
                () -> super.clearView(recyclerView, viewHolder),
                getAdapter().getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_ClearView"
        );
    }

    @Override
    public void onSelectedChanged(
            @Nullable RecyclerView.ViewHolder viewHolder,
            int actionState
    ) {
        getExceptionHandler().tryRun(
                () -> super.onSelectedChanged(viewHolder, actionState),
                getAdapter().getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_OnSelectedChanged"
        );
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        try {
            return super.onMove(recyclerView, viewHolder, target);
        } catch (Exception e) {
            getExceptionHandler().handleException(
                    e, getAdapter().getActivity().getSupportFragmentManager(),
                    this.getClass().getSimpleName() + "_OnMove"
            );
            return false;
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        getExceptionHandler().tryRun(
                () -> super.onSwiped(viewHolder, direction),
                getAdapter().getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName() + "_OnSwiped"
        );
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
