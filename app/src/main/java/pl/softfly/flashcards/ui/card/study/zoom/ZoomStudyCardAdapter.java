package pl.softfly.flashcards.ui.card.study.zoom;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import pl.softfly.flashcards.ui.card.study.display_ratio.DisplayRatioStudyCardFragment;
import pl.softfly.flashcards.ui.card.study.slide.SlideStudyCardAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class ZoomStudyCardAdapter extends SlideStudyCardAdapter {

    public ZoomStudyCardAdapter(FragmentActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
    }

    @Override
    public Fragment createFragment(int position) {
        return new ZoomStudyCardFragment(this, getDeckDbPath(), getCurrentList().get(position));
    }

    public ZoomStudyCardFragment getActiveFragment() {
        return (ZoomStudyCardFragment) super.getActiveFragment();
    }
}
