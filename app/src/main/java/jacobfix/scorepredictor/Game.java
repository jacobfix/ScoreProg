package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.Season;
import jacobfix.scorepredictor.util.Pair;
import jacobfix.scorepredictor.util.Util;

public class Game {

    private static final String TAG = Game.class.getSimpleName();

    protected String gameId;
    protected boolean isLocked;

    protected String awayAbbr;
    protected String homeAbbr;
    protected String awayName;
    protected String homeName;
    protected String awayLocale;
    protected String homeLocale;
    protected int awayColor;
    protected int homeColor;

    protected int awayScore;
    protected int homeScore;

    protected int quarter;

    protected long startTime;
    protected String startTimeDisplay;
    protected String dateDisplay;// Don't think this is needed

    protected int season;
    protected int week;
    protected Schedule.Day dayOfWeek;
    protected Season.SeasonType seasonType;
    protected Season.WeekType weekType;

    /* Synchronization lock. */
    protected final Object lock = new Object();

    protected boolean updatesRestricted = false;

    public static final SimpleDateFormat TIME_AND_DATE_FORMAT = new SimpleDateFormat("h:mm a@M/d");

    private static final int Q_PREGAME = -1;
    private static final int Q_HALFTIME = -2;
    private static final int Q_FINAL = -3;
    private static final int Q_FINAL_OT = -4;

    public enum State {
        PREGAME,
        IN_PROGRESS,
        FINAL
    }

    public Game() {}

    public Game(Game game) {
        synchronized (game.lock) {
            this.gameId = game.gameId;
            this.isLocked = game.isLocked;

            this.awayAbbr = game.awayAbbr;
            this.homeAbbr = game.homeAbbr;
            this.awayName = game.awayName;
            this.homeName = game.homeName;
            this.awayLocale = game.awayLocale;
            this.homeLocale = game.homeLocale;
            this.awayColor = game.awayColor;
            this.homeColor = game.homeColor;

            this.awayScore = game.awayScore;
            this.homeScore = game.homeScore;

            this.quarter = game.quarter;

            this.startTime = game.startTime;
            this.startTimeDisplay = game.startTimeDisplay;
            this.dateDisplay = game.dateDisplay;

            this.season = game.season;
            this.week = game.week;
            this.dayOfWeek = game.dayOfWeek;
            this.seasonType = game.seasonType;
            this.weekType = game.weekType;
        }
    }

    public void updateFromScheduleJson(JSONObject json) throws JSONException {
        synchronized (lock) {
            gameId = json.getString("gid");

            awayAbbr = json.getString("away");
            homeAbbr = json.getString("home");

            awayLocale = NflTeamProvider.getTeamLocale(awayAbbr);
            homeLocale = NflTeamProvider.getTeamLocale(homeAbbr);
            awayName = NflTeamProvider.getTeamName(awayAbbr);
            homeName = NflTeamProvider.getTeamName(homeAbbr);
            awayColor = NflTeamProvider.getTeamPrimaryColor(awayAbbr);
            homeColor = NflTeamProvider.getTeamPrimaryColor(homeAbbr);

            awayScore = json.getInt("away_score");
            homeScore = json.getInt("home_score");

            quarter = json.getInt("quarter");

            startTime = json.getLong("start_time");
            String timeAndDateString = Util.translateMillisSinceEpochToLocalDateString(startTime, TIME_AND_DATE_FORMAT);
            String[] split = timeAndDateString.split("@");
            startTimeDisplay = split[0];
            dateDisplay = split[1];

            week = json.getInt("week");

            dayOfWeek = Util.parseDayOfWeek(json.getString("day"));
            weekType = Util.parseWeekType(json.getString("type"), week);
            seasonType = Util.parseSeasonType(json.getString("season_segment"));
        }
    }

    public Object acquireLock() {
        return lock;
    }

    public void lock() {
        synchronized (lock) {
            isLocked = true;
        }
    }

    public String getId() {
        return gameId;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getStartTimeDisplay() {
        return startTimeDisplay;
    }

    public boolean isLocalStartTimePm() {
        return startTimeDisplay.split(" ")[1].equals("PM");
    }

    public String getDateDisplay() {
        return dateDisplay;
    }

    public int getQuarter() {
        return quarter;
    }

    public boolean isPregame() {
        return quarter == Q_PREGAME;
    }

    public boolean isFinal() {
        return quarter == Q_FINAL || quarter == Q_FINAL_OT;
    }

    public boolean isInProgress() {
        return !isPregame() && !isFinal();
    }

    public int getSeason() {
        return season;
    }

    public int getWeek() {
        return week;
    }

    public Schedule.Day getDayOfWeek() {
        return dayOfWeek;
    }

    public Season.SeasonType getSeasonType() {
        return seasonType;
    }

    public Season.WeekType getWeekType() {
        return weekType;
    }

    public String getAwayAbbr() {
        return awayAbbr;
    }

    public String getHomeAbbr() {
        return homeAbbr;
    }

    public String getAwayLocale() {
        return awayLocale;
    }

    public String getHomeLocale() {
        return homeLocale;
    }

    public String getAwayName() {
        return awayName;
    }

    public String getHomeName() {
        return homeName;
    }

    public int getAwayColor() {
        return awayColor;
    }

    public int getHomeColor() {
        return homeColor;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public static void disableUpdates(Game game) {
        synchronized (game.lock) {
            game.updatesRestricted = true;
        }
    }

    public static void enableUpdates(Game game) {
        synchronized (game.lock) {
            game.updatesRestricted = false;
        }
    }

    public enum Quarter {
        PREGAME,
        FIRST,
        SECOND,
        THIRD,
        FOURTH,
        OVERTIME,
        FINAL,
        FINAL_OVERTIME
    }

}
