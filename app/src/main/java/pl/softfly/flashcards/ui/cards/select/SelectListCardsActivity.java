package pl.softfly.flashcards.ui.cards.select;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.cards.drag_swipe.DragSwipeListCardsActivity;

public class SelectListCardsActivity extends DragSwipeListCardsActivity {

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected SelectCardBaseViewAdapter onCreateRecyclerViewAdapter() {
        return new SelectCardBaseViewAdapter(this, getDeckDbPath());
    }

    @NonNull
    @Override
    protected SelectCardTouchHelper onCreateTouchHelper() {
        return new SelectCardTouchHelper(getAdapter());
    }

    /* -----------------------------------------------------------------------------------------
     * Activity methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (getAdapter().isSelectionMode()) {
            menu.add(0, R.id.deselect_all, 2,
                    menuIconWithText(
                            getDrawableHelper(R.drawable.ic_baseline_deselect_24),
                            "Deselect all"
                    ));
            menu.add(0, R.id.delete_selected, 2,
                    menuIconWithText(
                            getDrawableHelper(R.drawable.ic_baseline_delete_sweep_24),
                            "Delete cards"
                    ));
            getMenuInflater().inflate(R.menu.menu_list_cards_select_mode, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deselect_all:
                getAdapter().onClickDeselectAll();
                return true;
            case R.id.delete_selected:
                getAdapter().onClickDeleteSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    @Override
    public SelectCardBaseViewAdapter getAdapter() {
        return (SelectCardBaseViewAdapter) super.getAdapter();
    }
}