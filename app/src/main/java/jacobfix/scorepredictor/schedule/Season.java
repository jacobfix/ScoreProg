package jacobfix.scorepredictor.schedule;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import jacobfix.scorepredictor.AtomicGame;
import jacobfix.scorepredictor.Game;
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

    public ArrayList<Week> getWeeks(SeasonType type) {
        return getSeasonSegment(type).getWeeks();
    }

    public ArrayList<Week> getWeeks(int type) {
        return getSeasonSegment(type).getWeeks();
    }

    /*
    public void putWeek(ScheduledGame[] games, int week, int seasonType) {

    }
    */

    public ArrayList<Game> populate(JSONObject seasonScheduleJson) throws JSONException {
        ArrayList<Game> created = new ArrayList<>();

        created.addAll(preseason.populate(seasonScheduleJson.getJSONArray("PRE")));
        created.addAll(regular.populate(seasonScheduleJson.getJSONArray("REG")));
        created.addAll(postseason.populate(seasonScheduleJson.getJSONArray("POST")));

        return created;
    }

    public void putWeek(String[] games, int week, int seasonType, WeekType weekType) {
        SeasonSegment segment = getSeasonSegment(seasonType);
        segment.putWeek(games, week, weekType);
    }

    public Week getWeek(int week, int type) {
        SeasonSegment segment = getSeasonSegment(type);
        return segment.getWeek(week);
    }

    public Week getWeek(int week, SeasonType type) {
        SeasonSegment segment = getSeasonSegment(type);
        return segment.getWeek(week);
    }

    public SeasonSegment getSeasonSegment(SeasonType type) {
        switch (type) {
            case PRE:
                return preseason;
            case REG:
                return regular;
            case POST:
                return postseason;
        }
        return null;
    }

    public int getNumberOfWeeks(SeasonType type) {
        SeasonSegment segment = getSeasonSegment(type);
        return segment.getNumberOfWeeks();
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

    public int getAbsoluteWeekNumber(int week, SeasonType type) {
        if (type == SeasonType.PRE) {
            return week;
        } else {
            return week - 1 + getNumberOfWeeks(SeasonType.PRE);
        }
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

        public ArrayList<Game> populate(JSONArray segmentScheduleJson) throws JSONException {
            ArrayList<Game> created = new ArrayList<>();

            for (int i = 0; i < segmentScheduleJson.length(); i++) {
                JSONObject weekScheduleJson = segmentScheduleJson.getJSONObject(i);

                int weekNumber = weekScheduleJson.getInt("week_number");
                WeekType weekType = parseWeekType(weekScheduleJson.getString("week_type"), weekNumber);

                JSONArray gamesJson = weekScheduleJson.getJSONArray("games");
                String[] weekGames = new String[gamesJson.length()];

                for (int j = 0; j < gamesJson.length(); j++) {
                    JSONObject gameJson = gamesJson.getJSONObject(j);

                    Game game = new Game();
                    game.updateFromScheduleJson(gameJson);

                    weekGames[j] = game.getId();
                    created.add(game);
                }

                putWeek(weekGames, weekNumber, weekType);
            }
            return created;
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

    private Season.WeekType parseWeekType(String type, int week) {
        switch(type) {
            case "PRE":
                if (week == 0) return Season.WeekType.HOF;
                return Season.WeekType.PRE;
            case "REG":
                return Season.WeekType.REG;
            case "WC":
                return Season.WeekType.WC;
            case "DIV":
                return Season.WeekType.DIV;
            case "CON":
                return Season.WeekType.CON;
            case "SB":
                return Season.WeekType.SB;
        }
        return null;
    }
}
