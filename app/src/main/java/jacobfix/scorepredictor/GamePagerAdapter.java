package jacobfix.scorepredictor;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class GamePagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = GamePagerAdapter.class.getSimpleName();

    private ArrayList<Integer> dynamicPageList;

    private static final String[] PAGE_TITLES = {"Friends", "Live", "Stats", "Compare"};

    public static final int FRIENDS_PAGE = 0;
    public static final int LIVE_PAGE = 1;
    public static final int STATS_PAGE = 2;
    public static final int COMPARE_PAGE = 3;

    /* Can probably get rid of all this "dynamic" stuff. */
    public GamePagerAdapter(FragmentManager fm) {
        super(fm);
        dynamicPageList = new ArrayList<Integer>();
        dynamicPageList.add(FRIENDS_PAGE);
        dynamicPageList.add(LIVE_PAGE);
        dynamicPageList.add(STATS_PAGE);
        dynamicPageList.add(COMPARE_PAGE);
    }

    @Override
    public int getCount() {
        return dynamicPageList.size();
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem() @ " + position);
        int pageId = dynamicPageList.get(position);
        switch (pageId) {
            case FRIENDS_PAGE:
                return FriendPredictionFragment.newInstance();
            case LIVE_PAGE:
                return PlayFeedFragment.newInstance();
            case STATS_PAGE:
                return StatsFragment.newInstance();
            case COMPARE_PAGE:
                return CompareFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(TAG, "getPageTitle()");
        return PAGE_TITLES[dynamicPageList.get(position)];
    }

    public void removePage(int position) {
        if (!dynamicPageList.isEmpty() && position < dynamicPageList.size()) {
            dynamicPageList.remove(position);
            notifyDataSetChanged();
        }
        Log.d(TAG, dynamicPageList.toString());
    }

    public void addPage(int position, int page) {
        dynamicPageList.add(position, page);
        notifyDataSetChanged();
        Log.d(TAG, dynamicPageList.toString());
    }

    public boolean isPageAt(int page, int position) {
        return dynamicPageList.get(position) == page;
    }
}
