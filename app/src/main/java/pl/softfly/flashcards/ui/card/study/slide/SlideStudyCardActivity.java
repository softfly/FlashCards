package pl.softfly.flashcards.ui.card.study.slide;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.reflect.Field;
import java.util.Objects;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.ui.base.IconInTopbarActivity;
import pl.softfly.flashcards.ui.card.EditCardActivity;
import pl.softfly.flashcards.ui.card.study.anim.StudyCardDepthPageTransformer;
import pl.softfly.flashcards.ui.card.study.display_ratio.DisplayRatioStudyCardAdapter;

public abstract class SlideStudyCardActivity extends IconInTopbarActivity {

    public static final String DECK_DB_PATH = "deckDbPath";

    private ViewPager2 viewPager;

    private SlideStudyCardAdapter adapter;

    private String deckDbPath;

    private DeckDatabase deckDb;

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_card_slide);

        Intent intent = getIntent();
        deckDbPath = intent.getStringExtra(DECK_DB_PATH);
        Objects.nonNull(deckDbPath);
        deckDb = getDeckDatabase(deckDbPath);

        viewPager = findViewById(R.id.pager);
        viewPager.setPageTransformer(new StudyCardDepthPageTransformer(this));

        adapter = new DisplayRatioStudyCardAdapter(this, deckDbPath);
        viewPager.setAdapter(adapter);
        reduceDragSensitivity(5);
    }

    protected void reduceDragSensitivity(int sensitivity) {
        try {
            Field ff = ViewPager2.class.getDeclaredField("mRecyclerView");
            ff.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) ff.get(viewPager);
            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * sensitivity);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Activity methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Menu options
     * ----------------------------------------------------------------------------------------- */

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_study_card, menu);
        menu.add(0, R.id.edit, 1,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_round_edit_24),
                        "Edit"
                ));
        menu.add(0, R.id.delete, 2,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_round_delete_24),
                        "Delete"
                ));
        menu.add(0, R.id.resetView, 3,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_empty),
                        "Reset view"
                ));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            startEditCardActivity();
            return true;
        } else if (item.getItemId() == R.id.delete) {
            adapter.getActiveFragment().deleteCard();
        } else if (item.getItemId() == R.id.resetView) {
            adapter.getActiveFragment().resetView();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void startEditCardActivity() {
        Intent intent = new Intent(this, EditCardActivity.class);
        intent.putExtra(EditCardActivity.DECK_DB_PATH, deckDbPath);
        intent.putExtra(EditCardActivity.CARD_ID, getAdapter().getActiveFragment().getCard().getId());
        this.startActivity(intent);
    }

    /* -----------------------------------------------------------------------------------------
     * Get/Sets
     * ----------------------------------------------------------------------------------------- */

    protected DeckDatabase getDeckDb() {
        return deckDb;
    }

    protected SlideStudyCardAdapter getAdapter() {
        return adapter;
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }
}