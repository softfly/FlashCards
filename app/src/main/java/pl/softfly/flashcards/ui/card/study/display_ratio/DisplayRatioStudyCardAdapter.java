package pl.softfly.flashcards.ui.card.study.display_ratio;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import pl.softfly.flashcards.ui.card.study.slide.SlideStudyCardAdapter;
import pl.softfly.flashcards.ui.card.study.zoom.ZoomStudyCardAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class DisplayRatioStudyCardAdapter extends ZoomStudyCardAdapter {

    public DisplayRatioStudyCardAdapter(FragmentActivity activity, String deckDbPath) {
        super(activity, deckDbPath);
    }

    @Override
    public Fragment createFragment(int position) {
        return new DisplayRatioStudyCardFragment(this, getDeckDbPath(), getCurrentList().get(position));
    }

    public DisplayRatioStudyCardFragment getActiveFragment() {
        return (DisplayRatioStudyCardFragment) super.getActiveFragment();
    }
}
