package ui.utils;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import java.lang.ref.WeakReference;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING;

public class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
    @NonNull
    private final WeakReference<TabLayout> tabLayoutRef;
    private int previousScrollState;
    private int scrollState;

    public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
        tabLayoutRef = new WeakReference<>(tabLayout);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        previousScrollState = scrollState;
        scrollState = state;
    }

    @Override
    public void onPageScrolled(
            final int position, final float positionOffset, final int positionOffsetPixels) {
        final TabLayout tabLayout = tabLayoutRef.get();
        if (tabLayout != null) {
            // Only update the text selection if we're not settling, or we are settling after
            // being dragged
            final boolean updateText =
                    scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
            // Update the indicator if we're not settling after being idle. This is caused
            // from a setCurrentItem() call and will be handled by an animation from
            // onPageSelected() instead.
            final boolean updateIndicator =
                    !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
            tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
        }
    }

    @Override
    public void onPageSelected(final int position) {
        final TabLayout tabLayout = tabLayoutRef.get();
        if (tabLayout != null
                && tabLayout.getSelectedTabPosition() != position
                && position < tabLayout.getTabCount()) {
            // Select the tab, only updating the indicator if we're not being dragged/settled
            // (since onPageScrolled will handle that).
            final boolean updateIndicator =
                    scrollState == SCROLL_STATE_IDLE
                            || (scrollState == SCROLL_STATE_SETTLING
                            && previousScrollState == SCROLL_STATE_IDLE);
            tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
        }
    }

    void reset() {
        previousScrollState = scrollState = SCROLL_STATE_IDLE;
    }
}
