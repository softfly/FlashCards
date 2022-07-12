package pl.softfly.flashcards.ui.card.study.slide;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.softfly.flashcards.db.room.DeckDatabase;
import pl.softfly.flashcards.ui.base.BaseFragmentStateAdapter;

/**
 * @author Grzegorz Ziemski
 */
public class SlideStudyCardAdapter extends BaseFragmentStateAdapter {

    private final List<Integer> cardIds = new LinkedList<>();
    private final DeckDatabase deckDb;
    private final String deckDbPath;
    private SlideStudyCardFragment activeFragment;

    public SlideStudyCardAdapter(FragmentActivity activity, String deckDbPath) {
        super(activity);
        this.deckDbPath = deckDbPath;
        this.deckDb = getDeckDatabase(deckDbPath);
        loadCards();
    }

    @Override
    public Fragment createFragment(int position) {
        return new SlideStudyCardFragment(this, getDeckDbPath(), getCurrentList().get(position));
    }

    public void loadCards() {
        getDeckDb().cardDaoAsync().getNextCardsToReplay()
                .subscribeOn(Schedulers.io())
                .subscribe(cards -> {
                    getCurrentList().clear();
                    getCurrentList().addAll(cards);
                }, this::onErrorLoadCards);
    }

    protected void onErrorLoadCards(Throwable e) {
        getExceptionHandler().handleException(
                e, getActivity().getSupportFragmentManager(),
                this.getClass().getSimpleName(),
                "Error while loading cards."
        );
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets Items
     * ----------------------------------------------------------------------------------------- */

    public void removeCardFromView() throws InterruptedException {
        int position = getActivity().getViewPager().getCurrentItem();
        getActivity().getViewPager().setCurrentItem(getActivity().getViewPager().getCurrentItem() + 1);
        Thread.sleep(300);
        getCurrentList().remove(getCurrentList().get(position));
        activity.runOnUiThread(() -> notifyItemRemoved(position));
    }

    public void showNextCard() {
        if (getActivity().getViewPager().getCurrentItem() == getCurrentList().size() - 1) {
            getActivity().getViewPager().setCurrentItem(0);
        } else {
            getActivity()
                    .getViewPager()
                    .setCurrentItem(getActivity().getViewPager().getCurrentItem() + 1);
        }
    }

    @NonNull
    protected List<Integer> getCurrentList() {
        return cardIds;
    }

    @Override
    public int getItemCount() {
        return cardIds.size();
    }

    @Override
    public long getItemId(int position) {
        return cardIds.get(position);
    }

    @Override
    public boolean containsItem(long itemId) {
        return cardIds.contains(itemId);
    }

    /* -----------------------------------------------------------------------------------------
     * Gets/Sets
     * ----------------------------------------------------------------------------------------- */

    protected void setActiveFragment(SlideStudyCardFragment fragment) {
        this.activeFragment = fragment;
    }

    protected DeckDatabase getDeckDb() {
        return deckDb;
    }

    protected String getDeckDbPath() {
        return deckDbPath;
    }

    protected SlideStudyCardActivity getActivity() {
        return (SlideStudyCardActivity) super.getActivity();
    }

    public SlideStudyCardFragment getActiveFragment() {
        return activeFragment;
    }
}
