package pl.softfly.flashcards.ui.deck.recent;

import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.main.MainActivity;
import pl.softfly.flashcards.ui.deck.standard.ListDecksFragment;

/**
 * @author Grzegorz Ziemski
 */
public class ListRecentDecksFragment extends ListDecksFragment {

    @Override
    protected RecentDeckBaseViewAdapter onCreateAdapter() {
        return new RecentDeckBaseViewAdapter((MainActivity) getActivity());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(0, R.id.settings, 5,
                menuIconWithText(
                        getDrawableHelper(R.drawable.ic_baseline_settings_24),
                        "Settings"
                ));
        inflater.inflate(R.menu.menu_recent_decks, menu);
    }

}