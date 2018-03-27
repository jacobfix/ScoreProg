package jacobfix.scoreprog.schedule;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.LockGameManager;
import jacobfix.scoreprog.server.ScheduleServerInterface;
import jacobfix.scoreprog.sync.GameProvider;

public class Schedule {

    private static final String TAG = Schedule.class.getSimpleName();

    private static HashMap<String, Game> games = new HashMap<>();

    private static HashMap<Integer, Season> seasons = new HashMap<>();

    private static HashSet<Season.Week> weeksToSync = new HashSet<>();

    private static boolean initialized;

    private static int currentYear;
    private static int currentWeekNumber;
    private static Season.SeasonType currentSeasonType;

    // TODO: Move these to the Season class
    public static final int PRE = 0;
    public static final int REG = 1;
    public static final int POS = 2;

    public static final int LOCK_AT_MINUTES_BEFORE = 5;

    private static final int MIN_PRESEASON_WEEK = 0;
    private static final int MAX_PRESEASON_WEEK = 7;

    private static final int MIN_REGSEASON_WEEK = 1;
    private static final int MAX_REGSEASON_WEEK = 20;

    private static final int MIN_POSTSEASON_WEEK = 16;
    private static final int MAX_POSTSEASON_WEEK = 24;

    private static final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;

    public static void init() throws Exception {
        JSONObject seasonStateJson = ScheduleServerInterface.getDefault().getCurrentSeasonStateJson();
        currentYear = seasonStateJson.getInt("year");
        Season season = loadSeason(currentYear);

        List<String> gamesToUpdate;
        currentWeekNumber = seasonStateJson.getInt("week");
        switch (seasonStateJson.getString("segment")) {
            case "PRE":
                currentSeasonType = Season.SeasonType.PRE;
                gamesToUpdate = season.getPreseason().getWeek(currentWeekNumber).getGames();
                break;

            case "REG":
                currentSeasonType = Season.SeasonType.REG;
                gamesToUpdate = season.getRegularSeason().getWeek(currentWeekNumber).getGames();
                break;

            case "POST":
                currentSeasonType = Season.SeasonType.POST;
                gamesToUpdate = season.getPostseason().getWeek(currentWeekNumber).getGames();
                break;

            default:
                throw new RuntimeException();
        }
        GameProvider.syncGamesInForeground(gamesToUpdate);
        seasons.put(currentYear, season);
    }

    public static Game getGame(String gid) {
        return games.get(gid);
    }

    public static synchronized boolean create() throws Exception {
        Bundle seasonState = ScheduleRetriever.get().getCurrentSeasonState();

        currentYear = seasonState.getInt("year");
        currentWeekNumber = seasonState.getInt("week");
        /*
        switch (seasonState.getInt("type")) {
            case 0:
                currentSeasonType = Schedule.PRE;
                break;
            case 1:
                currentSeasonType = Schedule.REG;
                break;
            case 2:
                currentSeasonType = Schedule.POS;
                break;
        }
        */

        loadSeason(currentYear);

        return initialized;
    }

    public static void lock(String gid) {
        Game game = GameProvider.getGame(gid);
        synchronized (game.acquireLock()) {
            game.lock();
        }
    }

    private static Season loadSeason(int year) throws Exception {
        Season season = new Season(year);

        JSONObject fullSeasonJson = ScheduleServerInterface.getDefault().getFullSeasonSchedule(year);
        ArrayList<Game> allGames = season.populate(fullSeasonJson);

        Log.d(TAG, "RRR number of postseason weeks: " + season.getPostseason().getNumberOfWeeks());
        Log.d(TAG, "RRR " + season.getPostseason().getWeek(20));
        Log.d(TAG, "RRR " + season.getPostseason().getWeek(23));

        for (Game game : allGames) {
            GameProvider.putGame(game);
            LockGameManager.get().scheduleLock(game.getId(), getLockTime(game.getStartTime()));

            if (!game.isFinal())
                GameProvider.addGameToSync(game.getId());
        }

        return season;
    }

    public static long getLockTime(long time) {
        Log.d(TAG, "getLockTime() called on " + time);
        return time - (LOCK_AT_MINUTES_BEFORE * 60000);
    }

    public static int getNumberOfWeeks(int season) {
        return getNumberOfWeeks(season, Schedule.PRE) + getNumberOfWeeks(season, Schedule.REG) + getNumberOfWeeks(season, Schedule.PRE);
    }

    public static int getNumberOfWeeks(int season, int type) {
        return seasons.get(season).getNumberOfWeeks(type);
    }

    /*
    public static Collection<String> getCurrentWeekGames() {
        return getAllGamesFrom(currentSeason, currentWeek, currentSeasonType);
    }
    */

    public static ArrayList<String> getAllGamesFrom(int season, int week, int type) {
        return seasons.get(season).getWeek(week, type).getGames();
    }

    public static Season getCurrentSeason() {
        return seasons.get(currentYear);
    }

    public static Season.Week getCurrentWeek() {
        return getCurrentSeason().getWeek(currentWeekNumber, currentSeasonType);
    }

    public static Season getSeason(int year) {
        return seasons.get(year);
    }

    public static int getCurrentYear() {
        return currentYear;
    }

    public static int getCurrentWeekNumber() {
        return currentWeekNumber;
    }

    public static Season.SeasonType getCurrentSeasonType() {
        return currentSeasonType;
    }

    /*
    public static int getCurrentSeasonType() {
        return currentSeasonType;
    }

    public static boolean postseason() {
        return currentSeasonType == Schedule.POS;
    }

    public static NflGame getGame(String gameId) {
        return (NflGame) games.get(gameId);
    }*/

    /* The granularity of a syncable item is an entire week. A week's worth of games is held
       in each "schedule" Web page, so when we sync, we may as well sync every game of the week. */
    public void addWeekToSync(int season, int week, int type) {
        weeksToSync.add(seasons.get(season).getWeek(week, type));
    }

    /*
    public class ScheduledGame implements ScheduledSyncHandler {

        private String gid;
        private String awayAbbr;
        private String homeAbbr;
        private String startTime;
        private String dayOfWeek;
        private String type;

        public String getId() {
            return null;
        }

        @Override
        public void sync(JSONObject json) throws JSONException {
            synchronized (this) {
                gid = json.getString("id");
                awayAbbr = json.getString("away");
                homeAbbr = json.getString("home");
                startTime = json.getString("time");
                dayOfWeek = json.getString("day");
                type = json.getString("type");
            }
        }
    }
    */

    public enum Day {
        SUN, MON, TUE, WED, THU, FRI, SAT
    }
}
