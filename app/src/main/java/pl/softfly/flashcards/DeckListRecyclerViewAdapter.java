package pl.softfly.flashcards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeckListRecyclerViewAdapter extends RecyclerView.Adapter<DeckListRecyclerViewAdapter.ViewHolder> {

    private final DeckListOnClickListener deckListOnClickListener;

    private final ArrayList<String> deckNames;

    public DeckListRecyclerViewAdapter(DeckListOnClickListener deckListOnClickListener, ArrayList<String> deckNames) {
        this.deckListOnClickListener = deckListOnClickListener;
        this.deckNames = deckNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_list_item, parent, false);
        return new ViewHolder(view, deckListOnClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.nameTextView.setText(deckNames.get(position));
        holder.nameTextView.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return deckNames.size();
    }

    public interface DeckListOnClickListener {
        void onDeckItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTextView;
        RelativeLayout deckLayoutListItem;
        DeckListOnClickListener deckListOnClickListener;

        public ViewHolder(View itemView, DeckListOnClickListener deckListOnClickListener) {
            super(itemView);
            this.deckListOnClickListener = deckListOnClickListener;
            nameTextView = itemView.findViewById(R.id.nameTextView);
            deckLayoutListItem = itemView.findViewById(R.id.deckListItem);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            deckListOnClickListener.onDeckItemClick(getAdapterPosition());
        }
    }
}
