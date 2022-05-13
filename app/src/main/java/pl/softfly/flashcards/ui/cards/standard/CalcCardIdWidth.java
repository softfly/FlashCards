package pl.softfly.flashcards.ui.cards.standard;

import android.view.ViewTreeObserver;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pl.softfly.flashcards.entity.Card;

/**
 * This sets the width of the ID column and the width of the header columns.
 * <p>
 * What problem does this solve?
 * I was unable to use the SDK to set the relative width of the ID column
 * to match with the length of the text.
 *
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
    public int getIdWidth(@NonNull List<Card> cards) {
        int length = calcMaxIdLength(cards);
        return getIdWidth(length);
    }

    private int calcMaxIdLength(@NonNull List<Card> cards) {
        return Integer.toString(cards.stream()
                .mapToInt(Card::getOrdinal)
                .max()
                .orElse(0)).length();
    }

    /**
     * ID.WIDTH.2. Use the id width if calculated. Use case end.
     */
    public void setIdWidth(@NonNull CardViewHolder holder, int width) {
        holder.getIdTextView().setLayoutParams(new TableRow.LayoutParams(
                width,
                TableRow.LayoutParams.MATCH_PARENT
        ));
    }

    /**
     * ID.WIDTH.3. If the id width has not been calculated, calculate id width.
     */
    @NonNull
    public ViewTreeObserver.OnDrawListener calcIdWidth(@NonNull RecyclerView recyclerView, @NonNull TextView idHeader) {
        return () -> {
            int width = findMaxIdWidth(recyclerView);
            if (width != 0) {
                setIdWidth(recyclerView, width);
                if (idHeader.getWidth() != width) {
                    idHeader.setWidth(width);
                }
            }
        };
    }

    private int findMaxIdWidth(@NonNull RecyclerView recyclerView) {
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

    private void setIdWidth(@NonNull RecyclerView recyclerView, int width) {
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
