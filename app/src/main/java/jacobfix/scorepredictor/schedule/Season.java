package jacobfix.scorepredictor.schedule;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import jacobfix.scorepredictor.NflGame;

public class Season {

    private static final String TAG = Season.class.getSimpleName();

    private int year;

    /*
    private SeasonSegment pre = new SeasonSegment(Schedule.PRE);
    private SeasonSegment reg = new SeasonSegment(Schedule.REG);
    private SeasonSegment post = new SeasonSegment(Schedule.POS);
    */

    private Preseason preseason = new Preseason();
    private RegularSeason regular = new RegularSeason();
    private Postseason postseason = new Postseason();

    public Season(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public ArrayList<Week> getWeeks(int type) {
        return getSeasonSegment(type).getWeeks();
    }

    /*
    public void putWeek(ScheduledGame[] games, int week, int seasonType) {

    }
    */

    public void putWeek(String[] games, int week, int seasonType, WeekType weekType) {
        SeasonSegment segment = getSeasonSegment(seasonType);
        segment.putWeek(games, week, weekType);
    }

    public Week getWeek(int week, int type) {
        SeasonSegment segment = getSeasonSegment(type);
        return segment.getWeek(week);
    }

    public int getNumberOfWeeks(int type) {
        SeasonSegment segment = getSeasonSegment(type);
        return segment.getNumberOfWeeks();
    }

    public int getWeekNumberWithinSegment(int position, int type) {
        SeasonSegment segment = getSeasonSegment(type);
        if (position < 1)
            return position;
        else if (position < 1 + preseason.getNumberOfWeeks())
            return position - 1;
        else if (position < preseason.getNumberOfWeeks() + regular.getNumberOfWeeks())
            return position - 1 - preseason.getNumberOfWeeks();
        else
            return position - 1 - preseason.getNumberOfWeeks() - regular.getNumberOfWeeks();
    }

    private SeasonSegment getSeasonSegment(int type) {
        /*
        switch (type) {
            case Schedule.PRE:
                return pre;
            case Schedule.REG:
                return reg;
            case Schedule.POS:
                return post;
        }
        return null;
        */
        switch (type) {
            case Schedule.PRE:
                return preseason;
            case Schedule.REG:
                return regular;
            case Schedule.POS:
                return postseason;
        }
        return null;
    }

    public class SeasonSegment {

        private int type;
        protected ArrayList<Week> weeks = new ArrayList<>();

        public SeasonSegment(int type) {
            this.type = type;
        }

        public Week getWeek(int weekNumber) {
            return weeks.get(weekNumber - 1);
        }

        public void putWeek(String[] games, int weekNumber, WeekType weekType) {
            weeks.add(weekNumber - 1, new Week(games, weekType, weekNumber));
        }

        public ArrayList<Week> getWeeks() {
            return weeks;
        }

        public int getNumberOfWeeks() {
            return weeks.size();
        }
    }

    public class Preseason extends SeasonSegment {

        public Preseason() {
            super(Schedule.PRE);
        }

        public Week getHofWeek() {
            return weeks.get(0);
        }

        @Override
        public void putWeek(String[] games, int weekNumber, WeekType weekType) {
            weeks.add(weekNumber, new Week(games, weekType, weekNumber));
        }

        public Week getWeek(int weekNumber) {
            return weeks.get(weekNumber);
        }
    }

    public class RegularSeason extends SeasonSegment {

        public RegularSeason() {
            super(Schedule.REG);
        }
    }

    public class Postseason extends SeasonSegment {

        public Postseason() {
            super(Schedule.POS);
        }


    }

    public class Week {

        private WeekType weekType;
        private int weekNumber;
        private ArrayList<String> games = new ArrayList<>();

        public Week(String[] games, WeekType type, int number) {
            weekType = type;
            weekNumber = number;
            this.games.addAll(Arrays.asList(games));
        }

        public ArrayList<String> getGames() {
            return games;
        }

        public WeekType getWeekType() {
            return weekType;
        }

        public int getWeekNumber() {
            return weekNumber;
        }
    }

    public enum SeasonType {
        PRE, REG, POST
    }

    public enum WeekType {
        HOF, PRE, REG, WC, DIV, CON, SB
    }
}
