package pl.softfly.flashcards.ui.card.study.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.color.MaterialColors;

import pl.softfly.flashcards.R;
import pl.softfly.flashcards.ui.card.study.anim.DepthPageTransformer;

/**
 * @author Grzegorz Ziemski
 */
public class StudyCardDepthPageTransformer extends DepthPageTransformer {

    private static final int MAX_ALPHA = 255;

    private static final int DP_2 = dpToPx(2);

    private final Context context;

    private final int cardBorderColor;

    private final GradientDrawable shapeDrawable;


    public StudyCardDepthPageTransformer(Context context) {
        this.context = context;
        cardBorderColor = MaterialColors.getColor(context, R.attr.cardBorder, "");
        shapeDrawable = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.ui_card_border_slide);
    }

    @Override
    protected void offToLeft(View view, float position) {
        setDefaultBackground(view);
        super.offToLeft(view, position);
    }

    @Override
    protected void moveToLeft(View view, float position) {
        int alpha = Math.min((int) (-position * MAX_ALPHA) + 50, MAX_ALPHA);
        shapeDrawable.setStroke(DP_2, ColorUtils.setAlphaComponent(cardBorderColor, alpha));
        view.setBackground(shapeDrawable);
        super.moveToLeft(view, position);
    }

    @Override
    protected void fullPage(View view, float position) {
        setDefaultBackground(view);
        super.fullPage(view, position);
    }

    @Override
    protected void moveToRight(View view, float position) {
        view.setBackground(ContextCompat.getDrawable(context, R.drawable.ui_card_border_slide));
        super.moveToRight(view, position);
    }

    @Override
    protected void offToRight(View view, float position) {
        setDefaultBackground(view);
        super.offToRight(view, position);
    }

    protected void setDefaultBackground(View view) {
        view.setBackground(new ColorDrawable(MaterialColors.getColor(view, android.R.attr.windowBackground)));
    }

    protected static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
