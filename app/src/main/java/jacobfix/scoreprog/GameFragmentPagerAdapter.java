package jacobfix.scoreprog;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

public class GameFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = GameFragmentPagerAdapter.class.getSimpleName();

    private SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();

    // private static final String[] PAGE_TITLES = {"Friends", "Live", "Stats", "Compare"};

    private static final String[] PAGE_TITLES = {"Predictions", "Drive Feed"};

    public static final int FRIENDS_PAGE = 0;
    public static final int LIVE_PAGE = 1;
    public static final int STATS_PAGE = 2;
    public static final int COMPARE_PAGE = 3;

    public GameFragmentPagerAdapter(FragmentManager fm) {
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
                // return FriendPredictionFragment.newInstance();
                return PredictionFragment.newInstance();
                // return StatsFragment.newInstance();
            case LIVE_PAGE:
                return PlayFeedFragment.newInstance();
                // return StatsFragment.newInstance();
                // return OriginalPlayFeedFragment.newInstance();
            case STATS_PAGE:
                Log.d(TAG, "NDS Stats Page");
                return StatsFragment.newInstance();
            case COMPARE_PAGE:
                Log.d(TAG, "NDS Compare Page");
                return CompareFragment.newInstance();
        }
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PAGE_TITLES[position];
    }

    public Fragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }
}
