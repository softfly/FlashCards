package pl.softfly.flashcards.ui.card.study.anim;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager2.widget.ViewPager2;

import pl.softfly.flashcards.R;

/**
 * @author Grzegorz Ziemski
 */
public class DepthPageTransformer implements ViewPager2.PageTransformer {

    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        if (position < -1) { // [-Infinity,-1)
            offToLeft(view, position);
        } else if (position < 0) { // [-1,0]
            moveToLeft(view, position);
        } else if (position == 0) {
            fullPage(view, position);
        } else if (position <= 1) { // (0,1]
            moveToRight(view, position);
        } else { // (1,+Infinity]
            offToRight(view, position);
        }
    }

    /**
     * This page is way off-screen to the left.
     */
    protected void offToLeft(View view, float position) {
        view.setAlpha(0f);
    }

    /**
     * Use the default slide transition when moving to the left page
     */
    protected void moveToLeft(View view, float position) {
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setTranslationZ(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);
    }

    /**
     * Front and center
     */
    protected void fullPage(View view, float position) { }

    protected void moveToRight(View view, float position) {
        // Fade the page out.
        view.setAlpha(1 - position);

        // Counteract the default slide transition
        view.setTranslationX(view.getWidth() * -position);
        // Move it behind the left page
        view.setTranslationZ(-1f);

        // Scale the page down (between MIN_SCALE and 1)
        float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);
    }

    /**
     * This page is way off-screen to the right.
     */
    protected void offToRight(View view, float position) {
        view.setAlpha(0f);
    }
}
