package jacobfix.scoreprog;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;

import jacobfix.scoreprog.schedule.Season;
import jacobfix.scoreprog.util.Util;

public class LobbyPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = LobbyPagerAdapter.class.getSimpleName();

    private ArrayList<Season.Week> weeks = new ArrayList<>();
    private SparseArray<LobbyFragment> registeredFragments = new SparseArray<>();

    private Season season;

    public LobbyPagerAdapter(FragmentManager fm, Season season) {
        super(fm);
        this.season = season;
        weeks.addAll(season.getWeeks(Season.SeasonType.PRE));
        weeks.addAll(season.getWeeks(Season.SeasonType.REG));
        weeks.addAll(season.getWeeks(Season.SeasonType.POST));
    }

    @Override
    public int getCount() {
        return weeks.size();
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem()");
        ArrayList<String> games = weeks.get(position).getGames();
        return LobbyFragment.newInstance(games.toArray(new String[games.size()]));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Season.Week week = weeks.get(position);
        return Util.getWeekTitle(week.getWeekType(), week.getWeekNumber());
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LobbyFragment createdFragment = (LobbyFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, createdFragment);
        return createdFragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public LobbyFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    public SparseArray<LobbyFragment> getRegisteredFragments() {
        return registeredFragments;
    }
}
