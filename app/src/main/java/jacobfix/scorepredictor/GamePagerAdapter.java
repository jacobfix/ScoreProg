package jacobfix.scorepredictor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

public class GamePagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = GamePagerAdapter.class.getSimpleName();

    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<Fragment>();

    private static final String[] PAGE_TITLES = {"Friends", "Live", "Stats", "Compare"};

    public static final int FRIENDS_PAGE = 0;
    public static final int LIVE_PAGE = 1;
    public static final int STATS_PAGE = 2;
    public static final int COMPARE_PAGE = 3;

    public GamePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
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
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(TAG, "instantiateItem()");
        Log.d(TAG, "Registering fragment @ " + position);
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PAGE_TITLES[position];
    }

    public Fragment getRegisteredFragment(int position) {
        Log.d(TAG, "getRegisteredFragment() @ position " + position);
        return mRegisteredFragments.get(position);
    }
}
