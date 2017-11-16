package jacobfix.scorepredictor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jacobfix.scorepredictor.schedule.Season;

public class GameActivityPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = GameActivityPagerAdapter.class.getSimpleName();

    private ArrayList<String> gameIds;
    private SparseArray<GameFragment> registeredFragments = new SparseArray<>();

    public GameActivityPagerAdapter(FragmentManager fm, String[] gameIds) {
        super(fm);
        this.gameIds = new ArrayList<>(Arrays.asList(gameIds));
    }

    @Override
    public int getCount() {
        return gameIds.size();
    }

    @Override
    public Fragment getItem(int position) {
        return GameFragment.newInstance(gameIds.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        GameFragment createdFragment = (GameFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, createdFragment);
        return createdFragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public GameFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
