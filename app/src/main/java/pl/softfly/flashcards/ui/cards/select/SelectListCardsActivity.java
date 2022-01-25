package pl.softfly.flashcards.ui.cards.select;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.cards.drag_swipe.DragSwipeListCardsActivity;

public class SelectListCardsActivity extends DragSwipeListCardsActivity {

    private SelectCardRecyclerViewAdapter adapter;

    @Override
    protected SelectCardRecyclerViewAdapter onCreateRecyclerViewAdapter() {
        adapter = new SelectCardRecyclerViewAdapter(this, getDeckName());
        setAdapter(adapter);
        return adapter;
    }

    @Override
    protected ItemTouchHelper.Callback onCreateTouchHelper() {
        return new SelectCardTouchHelper(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        if (adapter.isSelectionMode()) {
            menu.add(0, R.id.deselect_all, 2,
                    menuIconWithText(
                            getDrawable(R.drawable.ic_baseline_deselect_24),
                            "Deselect all"
                    ));
            menu.add(0, R.id.delete_selected, 2,
                    menuIconWithText(
                            getDrawable(R.drawable.ic_baseline_delete_sweep_24),
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
                adapter.onClickDeselectAll();
                return true;
            case R.id.delete_selected:
                adapter.onClickDeleteSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void setAdapter(SelectCardRecyclerViewAdapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
    }
}