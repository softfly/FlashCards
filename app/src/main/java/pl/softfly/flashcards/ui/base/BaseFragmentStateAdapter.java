package pl.softfly.flashcards.ui.base;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import pl.softfly.flashcards.ExceptionHandler;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.DeckDatabase;

/**
 * @author Grzegorz Ziemski
 */
public abstract class BaseFragmentStateAdapter extends FragmentStateAdapter {

    protected FragmentActivity activity;

    public BaseFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        activity = fragmentActivity;
    }

    public BaseFragmentStateAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public BaseFragmentStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    protected DeckDatabase getDeckDatabase(String dbPath) {
        return DeckDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getDatabase(dbPath);
    }

    protected FragmentActivity getActivity() {
        return activity;
    }

    protected ExceptionHandler getExceptionHandler() {
        return ExceptionHandler.getInstance();
    }
}
