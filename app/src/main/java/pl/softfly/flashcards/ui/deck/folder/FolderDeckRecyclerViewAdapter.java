package pl.softfly.flashcards.ui.deck.folder;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.AppDatabaseUtil;
import pl.softfly.flashcards.ui.deck.DeckRecyclerViewAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class FolderDeckRecyclerViewAdapter extends DeckRecyclerViewAdapter {

    private static final int VIEW_TYPE_FOLDER = 2;

    private final ArrayList<File> folders = new ArrayList<>();

    public FolderDeckRecyclerViewAdapter(@NonNull AppCompatActivity activity, File currentFolder) {
        super(activity, currentFolder);
    }

    @Override
    public void loadItems(@NonNull File path) {
        deckNames.clear();
        deckNames.addAll(AppDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getStorageDb()
                .listDatabases(path)
        );
        folders.clear();
        folders.addAll(AppDatabaseUtil
                .getInstance(activity.getApplicationContext())
                .getStorageDb()
                .listFolders(path)
        );
        activity.runOnUiThread(() -> notifyDataSetChanged());
    }

    @Override
    public int getItemViewType(int position) {
        if (folders.size() > position) return VIEW_TYPE_FOLDER;
        else return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (VIEW_TYPE_FOLDER == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
            return new FolderViewHolder(view, this);
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (folders.size() > position) {
            FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
            File folder = folders.get(position);
            folderViewHolder.nameTextView.setText(folder.getName());
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onItemClick(int itemPosition) {
        if (folders.size() > itemPosition) {
            openFolder(itemPosition);
        } else {
            super.onItemClick(itemPosition);
        }
    }

    private void openFolder(int folderPosition) {
        Intent intent = new Intent(activity, ListFoldersDecksActivity.class);
        intent.putExtra(ListFoldersDecksActivity.CURRENT_FOLDER_PATH, folders.get(folderPosition).getPath());
        activity.startActivity(intent);
    }

    public void showDeleteFolderDialog(int folderPosition) {
        DeleteFolderDialog dialog = new DeleteFolderDialog(folders.get(folderPosition));
        dialog.show(activity.getSupportFragmentManager(), DeleteFolderDialog.class.getSimpleName());
    }

    @Override
    public int getItemCount() {
        return folders.size() + super.getItemCount();
    }

    protected int getDeckPosition(int itemPosition) {
        return itemPosition - folders.size();
    }
}
