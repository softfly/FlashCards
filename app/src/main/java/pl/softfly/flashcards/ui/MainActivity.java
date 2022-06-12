package pl.softfly.flashcards.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.CreateSampleDeck;
import pl.softfly.flashcards.R;
import pl.softfly.flashcards.databinding.ActivityMainBinding;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.db.room.AppDatabase;
import pl.softfly.flashcards.entity.AppConfig;
import pl.softfly.flashcards.ui.deck.folder.ListFoldersDecksFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ListFoldersDecksFragment listDecksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.recent_decks, R.id.list_decks)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        NavHostFragment nav = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        for (Fragment f : nav.getChildFragmentManager().getFragments()) {
            if (f instanceof ListFoldersDecksFragment) {
                listDecksFragment = (ListFoldersDecksFragment) f;
            }
        }
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
                () -> listDecksFragment.getAdapter().refreshItems()
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        listDecksFragment.onSupportNavigateUp();
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
                .getAppDatabase();
    }

    public ListFoldersDecksFragment getListDecksFragment() {
        return listDecksFragment;
    }
}