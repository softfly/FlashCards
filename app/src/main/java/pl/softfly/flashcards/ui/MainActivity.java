package pl.softfly.flashcards.ui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CreateSampleDeck;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.ActivityMainBinding;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.entity.app.AppConfig;
import pl.softfly.flashcards.ui.deck.folder.ListFoldersDecksFragment;
import pl.softfly.flashcards.ui.deck.recent.ListRecentDecksFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Fragment currentFragment;

    private ListFoldersDecksFragment listFoldersDecksFragment;

    private ListRecentDecksFragment listRecentDecksFragment;

    private int startingPosition = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;
        navView.setOnItemSelectedListener(this::onNavigationItemSelected);

        getAppDatabase().deckDaoAsync().findByLastUpdatedAt(1)
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .doOnSuccess(deck -> {
                    if (deck.size() > 0) {
                        currentFragment = getListRecentDecksFragment();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, currentFragment);
                        transaction.commit();
                        binding.navView.setSelectedItemId(R.id.recent_decks);
                        startingPosition = 1;
                    } else {
                        currentFragment = getListAllDecksFragment();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, currentFragment);
                        transaction.commit();
                        binding.navView.setSelectedItemId(R.id.list_decks);
                        startingPosition = 2;
                    }
                }).subscribe();
    }

    private boolean onNavigationItemSelected(MenuItem menuItem) {
        Fragment fragment = null;
        int newPosition = 0;
        switch (menuItem.getItemId()) {
            case R.id.recent_decks:
                fragment = getListRecentDecksFragment();
                newPosition = 1;
                break;
            case R.id.list_decks:
                fragment = getListAllDecksFragment();
                newPosition = 2;
                break;
        }
        loadFragment(fragment, newPosition);
        return true;
    }

    /**
     * Adds a slide animation when switching between tabs.
     */
    protected void loadFragment(Fragment fragment, int newPosition) {
        if(startingPosition > newPosition) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
            transaction.replace(R.id.container, fragment);
            transaction.commit();
        }
        if(startingPosition < newPosition) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left );
            transaction.replace(R.id.container, fragment);
            transaction.commit();
        }
        startingPosition = newPosition;
        currentFragment = fragment;
    }

    @Override
    protected void onResume() {
        initDarkMode();
        super.onResume();
        createSampleDeck();
    }

    /**
     * It is not needed elsewhere, because
     * 1) this is the first activity
     * 2) after changing the app settings, it comes back to this activity
     */
    protected void initDarkMode() {
        getAppDatabase().appConfigAsync().findByKey(AppConfig.DARK_MODE)
                .subscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .subscribe(appConfig -> {
                    switch (appConfig.getValue()) {
                        case AppConfig.DARK_MODE_ON:
                            runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));
                            break;
                        case AppConfig.DARK_MODE_OFF:
                            runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO));
                            break;
                        default:
                            runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
                            break;
                    }
                });
    }

    protected void createSampleDeck() {
        (new CreateSampleDeck()).create(
                this.getApplicationContext(),
                () -> {
                    if (isListAllDecksFragment()) {
                        getListAllDecksFragment().getAdapter().refreshItems();
                    }
                }
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (isListAllDecksFragment()) {
            getListAllDecksFragment().onSupportNavigateUp();
        }
        return true;
    }

    public void showBackArrow() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void hideBackArrow() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    @Nullable
    protected AppDatabase getAppDatabase() {
        return AppDatabaseUtil
                .getInstance(getApplicationContext())
                .getDatabase();
    }

    public ListFoldersDecksFragment getListAllDecksFragment() {
        if (listFoldersDecksFragment == null) {
            listFoldersDecksFragment = new ListFoldersDecksFragment();
        }
        return listFoldersDecksFragment;
    }

    public ListRecentDecksFragment getListRecentDecksFragment() {
        if (listRecentDecksFragment == null) {
            listRecentDecksFragment = new ListRecentDecksFragment();
        }
        return listRecentDecksFragment;
    }

    protected boolean isListAllDecksFragment() {
        return currentFragment instanceof ListFoldersDecksFragment;
    }

    protected boolean isListRecentDecksFragment() {
        return currentFragment instanceof ListRecentDecksFragment;
    }
}