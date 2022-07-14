package pl.softfly.flashcards.ui.deck.standard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.softfly.flashcards.R;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;

/**
 * @author Grzegorz Ziemski
 */
public class DeckBottomMenu extends BottomSheetDialogFragment {

    protected DeckViewAdapter adapter;

    protected int position;

    public DeckBottomMenu(DeckViewAdapter adapter, int position) {
        this.adapter = adapter;
        this.position = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_deck_menu, container, false);

        NavigationView navigationView = view.findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            dismiss();
            switch (item.getItemId()) {
                case R.id.exportDbDeck:
                    adapter.launchExportDb(position);
                    return true;
                case R.id.exportFile:
                    adapter.launchExportToFile(position);
                    return true;
                case R.id.sync:
                    adapter.launchSyncFile(position);
                    return true;
            }

            return false;
        });
        return view;
    }


}
