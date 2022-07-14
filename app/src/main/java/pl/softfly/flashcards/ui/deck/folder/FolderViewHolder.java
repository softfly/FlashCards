package pl.softfly.flashcards.ui.deck.folder;

import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.base.recyclerview.BaseViewHolder;

/**
 * @author Grzegorz Ziemski
 */
public class FolderViewHolder extends BaseViewHolder implements View.OnClickListener {

    public TextView nameTextView;
    TextView moreTextView;
    RelativeLayout deckLayoutListItem;
    FolderDeckViewAdapter adapter;

    public FolderViewHolder(@NonNull View itemView, FolderDeckViewAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        nameTextView = itemView.findViewById(R.id.nameTextView);
        deckLayoutListItem = itemView.findViewById(R.id.deckListItem);
        initMoreTextView();
        itemView.setOnClickListener(this);
    }

    protected void initMoreTextView() {
        moreTextView = itemView.findViewById(R.id.moreTextView);
        moreTextView.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), moreTextView);
            popup.getMenuInflater().inflate(R.menu.popup_menu_folder, popup.getMenu());
            popup.setOnMenuItemClickListener(this::onMenuMoreClick);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popup.setForceShowIcon(true);
            popup.show();
        });
    }

    protected boolean onMenuMoreClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove:
                adapter.showDeleteFolderDialog(getBindingAdapterPosition());
                return true;
            case R.id.cut:
                adapter.cut(getBindingAdapterPosition());
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        adapter.onItemClick(getBindingAdapterPosition());
    }

    public FolderDeckViewAdapter getAdapter() {
        return adapter;
    }
}