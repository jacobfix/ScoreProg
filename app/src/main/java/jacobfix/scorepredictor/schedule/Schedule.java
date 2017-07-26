package jacobfix.scorepredictor.schedule;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.GameRetriever;
import jacobfix.scorepredictor.LockGameManager;
import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.sync.GameCache;
import jacobfix.scorepredictor.sync.GameProvider;
import jacobfix.scorepredictor.sync.GameSyncableMap;
import jacobfix.scorepredictor.sync.JsonProvider;
import jacobfix.scorepredictor.sync.SyncableMap;
import jacobfix.scorepredictor.task.BaseTask;

public class Schedule {

    private static final String TAG = Schedule.class.getSimpleName();

    private static ScheduleCache scheduleCache;
    /* Get the games from Schedule. */

    private static GameCache gameCache = new GameCache();

    /* Contains all of the games, mapped from game IDs to game objects. */
    // private static HashMap<String, NflGame> games = new HashMap<>();
    // private static SyncableMap<String, NflGame> games = new SyncableMap<>();
    private static GameSyncableMap games = new GameSyncableMap();

    private static HashMap<Integer, Season> seasons = new HashMap<>();

    private static HashSet<Season.Week> weeksToSync = new HashSet<>();

    private static boolean initialized;

    private static int currentSeason;
    private static int currentWeek;
    private static int currentSeasonType;

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

    public static synchronized boolean init() throws Exception {
        BaseTask task = new BaseTask() {
            @Override
            public void execute() {

            }
        };

        Bundle seasonState = ScheduleRetriever.get().getCurrentSeasonState();

        currentSeason = seasonState.getInt("year");
        currentWeek = seasonState.getInt("week");

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

        loadSeason(currentSeason);
        // loadSeason(2016);

        return initialized;
    }

    public static void lock(String gid) {
        NflGame game = (NflGame) games.get(gid);
        synchronized (game) {
            game.lock();
        }
    }

    private static void loadSeason(int year) throws Exception {
        Log.d(TAG, "loadSeason()");
        Season season = new Season(year);

        for (int week = MIN_PRESEASON_WEEK; week < MAX_PRESEASON_WEEK; week++)
        // for (int week = MIN_PRESEASON_WEEK; week < 2; week++)
            loadWeek(season, week, Schedule.PRE);

        /*
        for (int week = MIN_REGSEASON_WEEK; week < MAX_REGSEASON_WEEK; week++)
            loadWeek(season, week, Schedule.REG);

        if (postseason())
            for (int week = MIN_POSTSEASON_WEEK; week < MAX_POSTSEASON_WEEK; week++)
                loadWeek(season, week, Schedule.POS);
        */

        seasons.put(year, season);
    }

    public static boolean loadWeek(Season season, int week, int seasonType) throws Exception {
        Log.d(TAG, "Loading Season " + season + ", Week " + week + ", " + seasonType);

        NflGame[] gamesOfWeek = games.syncInForeground(season.getYear(), week, seasonType, false);

        if (gamesOfWeek.length == 0)
            return false;

        for (NflGame game : gamesOfWeek)
            if (game.getStartTime() < LockGameManager.get().now() + MILLISECONDS_IN_DAY)
                game.syncFullDetails(GameRetriever.getGameJson(game.getGameId()));


        String[] gameIds = new String[gamesOfWeek.length];
        for (int i = 0; i < gamesOfWeek.length; i++) {
            NflGame game = gamesOfWeek[i];

            LockGameManager.get().scheduleLock(game.getGameId(), getLockTime(game.getStartTime()));
            gameIds[i] = gamesOfWeek[i].getGameId();
        }
        season.putWeek(gameIds, week, seasonType, gamesOfWeek[0].getWeekType());
        return true;

        /*
        // games.sync(season.getYear(), week, seasonType);
        ScheduledGame[] scheduledGames = ScheduleRetriever.get().getWeek(season.getYear(), week, seasonType);

        Log.d(TAG, "# of games loaded for week " + week + ", " + seasonType + ": " + scheduledGames.length);
        if (scheduledGames.length == 0)
            return false;

        String[] gameIds = new String[scheduledGames.length];
        for (int i = 0; i < scheduledGames.length; i++) {
            NflGame game = new NflGame(scheduledGames[i]);
            gameIds[i] = game.getGameId();

            // TODO: Get the JSON of all of *this* week's games, because those are the ones the user is most likely to select. Load the others on-demand
            if (game.getStartTime() < LockGameManager.get().now() + MILLISECONDS_IN_DAY) {
                JSONObject json = GameRetriever.getGameJson(game.getGameId());
                if (json != null)
                    game.sync(json);
            }

            games.put(game.getGameId(), game);
            LockGameManager.get().scheduleLock(game.getGameId(), getLockTime(game.getStartTime()));
        }

        // Week w = new Week(gameIds, week, seasonType, weekType);
        season.putWeek(gameIds, week, seasonType, scheduledGames[0].weekType);
        return true;
        */
    }

    /* public static String[] initializeGames(ScheduledGame[] scheduledGames) {
        String[] gameIds = new String[scheduledGames.length];
        for (int i = 0; i < scheduledGames.length; i++) {
            NflGame game = new NflGame(scheduledGames[i]);

            gameIds[i] = game.getGameId();
            games.put(game.getGameId(), game);

            LockGameManager.get().scheduleLock(game.getGameId(), getLockTime(game.getStartTime()));
        }
        return gameIds;
    } */

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

    public static Collection<String> getCurrentWeekGames() {
        return getAllGamesFrom(currentSeason, currentWeek, currentSeasonType);
    }

    public static ArrayList<String> getAllGamesFrom(int season, int week, int type) {
        return seasons.get(season).getWeek(week, type).getGames();
    }

    public static Season getSeason(int year) {
        return seasons.get(year);
    }

    public static int getCurrentSeason() {
        return currentSeason;
    }

    public static int getCurrentWeek() {
        return currentWeek;
    }

    public static int getCurrentSeasonType() {
        return currentSeasonType;
    }

    public static boolean postseason() {
        return currentSeasonType == Schedule.POS;
    }

    public static NflGame getGame(String gameId) {
        return (NflGame) games.get(gameId);
    }

    /* The granularity of a syncable item is an entire week. A week's worth of games is held
       in each "schedule" Web page, so when we sync, we may as well sync every game of the week. */
    public void addWeekToSync(int season, int week, int type) {
        weeksToSync.add(seasons.get(season).getWeek(week, type));
    }

    public void clearWeeksToSync() {
        weeksToSync.clear();
    }

    public static void registerScheduleSyncListener(AsyncCallback<ScheduledGame[]> listener) {
        scheduleCache.registerSyncListener(listener);
    }

    public static void unregisterScheduleSyncListener(AsyncCallback<ScheduledGame[]> listener) {
        scheduleCache.unregisterSyncListener(listener);
    }

    /*
    public class ScheduledGame implements Syncable {

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

    public enum SeasonType {
        PRE, REG, POS
    }

    public enum Day {
        SUN, MON, TUE, WED, THU, FRI, SAT
    }
}
