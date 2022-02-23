package pl.softfly.flashcards.ui.cards.standard;

import android.view.ViewTreeObserver;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pl.softfly.flashcards.entity.Card;

/**
 * @author Grzegorz Ziemski
 */
public class CalcCardIdWidth {

    private static CalcCardIdWidth INSTANCE;

    private final ArrayList<Integer> idWidthPerLength = new ArrayList<>(3);

    public static synchronized CalcCardIdWidth getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CalcCardIdWidth();
        }
        return INSTANCE;
    }

    private int getIdWidth(int length) {
        if (length == 0) return 0;
        if (length > idWidthPerLength.size()) return 0;
        Integer width = idWidthPerLength.get(length - 1);
        return width != null ? width : 0;
    }

    private void setIdWidth(int length, int width) {
        idWidthPerLength.add(length - 1, width);
    }

    /**
     * ID.WIDTH.1. Check if the id width has been calculated for the currently used max id length.
     */
    public int getIdWidth(List<Card> cards) {
        int length = calcMaxIdLength(cards);
        return getIdWidth(length);
    }

    private int calcMaxIdLength(List<Card> cards) {
        return Integer.toString(cards.stream()
                .mapToInt(Card::getOrdinal)
                .max()
                .orElse(0)).length();
    }

    /**
     * ID.WIDTH.2. Use the id width if calculated. Use case end.
     */
    public void setIdWidth(CardViewHolder holder, int width) {
        holder.getIdTextView().setLayoutParams(new TableRow.LayoutParams(
                width,
                TableRow.LayoutParams.MATCH_PARENT
        ));
    }

    /**
     * ID.WIDTH.3. If the id width has not been calculated, calculate id width.
     */
    public ViewTreeObserver.OnDrawListener calcIdWidth(RecyclerView recyclerView, TextView idHeader) {
        return () -> {
            int width = findMaxIdWidth(recyclerView);
            setIdWidth(recyclerView, width);
            idHeader.setWidth(width);
        };
    }

    private int findMaxIdWidth(RecyclerView recyclerView) {
        int maxIdWidth = 0;
        for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            CardViewHolder holder = (CardViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (holder.getIdTextView().getWidth() > maxIdWidth) {
                maxIdWidth = holder.getIdTextView().getWidth();
                setIdWidth(holder.getIdTextView().length(), maxIdWidth);
            }
        }
        return maxIdWidth;
    }

    private void setIdWidth(RecyclerView recyclerView, int width) {
        for (int childCount = recyclerView.getChildCount(), i = 0; i < childCount; ++i) {
            CardViewHolder holder = (CardViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (holder.getIdTextView().getWidth() != width) {
                holder.getIdTextView().setLayoutParams(new TableRow.LayoutParams(
                        width,
                        TableRow.LayoutParams.MATCH_PARENT
                ));
            }
        }
    }
}
