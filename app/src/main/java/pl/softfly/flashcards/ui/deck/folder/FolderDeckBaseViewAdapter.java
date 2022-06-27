package pl.softfly.flashcards.ui.deck.folder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.db.DeckDatabaseUtil;
import pl.softfly.flashcards.ui.main.MainActivity;
import pl.softfly.flashcards.ui.deck.standard.DeckBaseViewAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class FolderDeckBaseViewAdapter extends DeckBaseViewAdapter {

    private static final int VIEW_TYPE_FOLDER = 2;

    private final ArrayList<File> folders = new ArrayList<>();

    private File currentFolder = getRootFolder();

    private final MutableLiveData<String> cutPathLiveData = new MutableLiveData<>();

    /* -----------------------------------------------------------------------------------------
     * Constructor
     * ----------------------------------------------------------------------------------------- */

    public FolderDeckBaseViewAdapter(@NonNull MainActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (VIEW_TYPE_DECK == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck, parent, false);
            return onCreateFolderViewHolder(view);
        } else if (VIEW_TYPE_FOLDER == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
            return onCreateDeckViewHolder(view);
        } else {
            throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    protected RecyclerView.ViewHolder onCreateFolderViewHolder(View view) {
        return new FolderDeckViewHolder(view, this);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateDeckViewHolder(View view) {
        return new FolderViewHolder(view, this);
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

    /* -----------------------------------------------------------------------------------------
     * Adapter methods overridden
     * ----------------------------------------------------------------------------------------- */

    @Override
    public int getItemViewType(int position) {
        if (folders.size() > position) return VIEW_TYPE_FOLDER;
        else return super.getItemViewType(position);
    }

    /* -----------------------------------------------------------------------------------------
     * Items actions
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void loadItems(@NonNull File folder) {
        if (countSeparators(getRootFolder()) > countSeparators(folder)) {
            throw new RuntimeException("Folders lower than the root folder for the storage databases cannot be opened.");
        } else {
            currentFolder = folder;
            deckNames.clear();
            deckNames.addAll(DeckDatabaseUtil
                    .getInstance(getActivity().getApplicationContext())
                    .getStorageDb()
                    .listDatabases(folder)
            );
            folders.clear();
            folders.addAll(DeckDatabaseUtil
                    .getInstance(getActivity().getApplicationContext())
                    .getStorageDb()
                    .listFolders(folder)
            );
            getActivity().runOnUiThread(() -> notifyDataSetChanged());
        }
    }

    protected long countSeparators(File file) {
        return file.getPath().chars().filter(ch -> ch == File.separatorChar).count();
    }

    protected String getFullFolderPath(int itemPosition) {
        return folders.get(itemPosition).getPath();
    }

    protected boolean isRootFolder() {
        return getRootFolder().equals(currentFolder);
    }

    protected boolean isFolder(int itemPosition) {
        return folders.size() > itemPosition;
    }

    /* -----------------------------------------------------------------------------------------
     * Actions
     * ----------------------------------------------------------------------------------------- */

    @Override
    public void onItemClick(int itemPosition) {
        if (folders.size() > itemPosition) {
            openFolder(itemPosition);
        } else {
            super.onItemClick(itemPosition);
        }
    }

    protected void openFolder(int folderPosition) {
        loadItems(new File(folders.get(folderPosition).getPath()));
        if (isRootFolder()) {
            getActivity().hideBackArrow();
        } else {
            getActivity().showBackArrow();
        }
    }

    public void showDeleteFolderDialog(int folderPosition) {
        DeleteFolderDialog dialog = new DeleteFolderDialog(folders.get(folderPosition), this);
        dialog.show(getActivity().getSupportFragmentManager(), DeleteFolderDialog.class.getSimpleName());
    }

    public void cut(int itemPosition) {
        if (isFolder(itemPosition)) {
            cutPathLiveData.postValue(getFullFolderPath(itemPosition));
        } else {
            cutPathLiveData.postValue(getFullDeckPath(itemPosition));
        }
    }

    public void goFolderUp() {
        loadItems(currentFolder.getParentFile());
        if (isRootFolder()) {
            getActivity().hideBackArrow();
        } else {
            getActivity().showBackArrow();
        }
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    @Override
    public int getItemCount() {
        return folders.size() + super.getItemCount();
    }

    @Override
    protected int getDeckPosition(int itemPosition) {
        return itemPosition - folders.size();
    }

    @Override
    public File getCurrentFolder() {
        return currentFolder;
    }

    public MutableLiveData<String> getCutPathLiveData() {
        return cutPathLiveData;
    }
}
