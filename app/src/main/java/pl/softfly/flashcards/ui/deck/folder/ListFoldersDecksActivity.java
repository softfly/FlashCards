package pl.softfly.flashcards.ui.deck.folder;

import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.io.File;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.deck.ListDecksActivity;

/**
 * @author Grzegorz Ziemski
 */
public class ListFoldersDecksActivity extends ListDecksActivity {

    public static final String CURRENT_FOLDER_PATH = "currentFolderPath";

    @Override
    protected void initCurrentFolder() {
        Intent intent = getIntent();
        String currentFolderPath = intent.getStringExtra(CURRENT_FOLDER_PATH);
        if (currentFolderPath != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            currentFolder = new File(currentFolderPath);
        } else {
            super.initCurrentFolder();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_folder: {
                DialogFragment dialog = new CreateFolderDialog(currentFolder);
                dialog.show(this.getSupportFragmentManager(), "CreateFolder");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}