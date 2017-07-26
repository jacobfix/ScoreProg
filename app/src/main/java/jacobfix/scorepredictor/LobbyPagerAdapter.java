package jacobfix.scorepredictor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.Season;

public class LobbyPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = LobbyPagerAdapter.class.getSimpleName();

    private ArrayList<Season.Week> weeks = new ArrayList<>();

    private Season season;

    public LobbyPagerAdapter(FragmentManager fm, Season season) {
        super(fm);
        this.season = season;
        weeks.addAll(season.getWeeks(Schedule.PRE));
        Log.d(TAG, "After adding preseason weeks: " + weeks.size());
        weeks.addAll(season.getWeeks(Schedule.REG));
        weeks.addAll(season.getWeeks(Schedule.POS));
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
        switch (week.getWeekType()) {
            case HOF:
                return "Hall of Fame";
            case PRE:
                return "Preseason Week " + week.getWeekNumber();
            case REG:
                return "Week " + week.getWeekNumber();
            case WC:
                return "Wild Card";
            case DIV:
                return "Division";
            case CON:
                return "Conference";
            case SB:
                return "Super Bowl";
        }
        return new String();
    }
}
