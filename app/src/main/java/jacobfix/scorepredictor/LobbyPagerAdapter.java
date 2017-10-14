package jacobfix.scorepredictor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.Season;
import jacobfix.scorepredictor.util.Util;

public class LobbyPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = LobbyPagerAdapter.class.getSimpleName();

    private ArrayList<Season.Week> weeks = new ArrayList<>();

    private Season season;

    public LobbyPagerAdapter(FragmentManager fm, Season season) {
        super(fm);
        this.season = season;
        weeks.addAll(season.getWeeks(Season.SeasonType.PRE));
        // weeks.addAll(season.getWeeks(Schedule.PRE));
        Log.d(TAG, "After adding preseason weeks: " + weeks.size());
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
}
