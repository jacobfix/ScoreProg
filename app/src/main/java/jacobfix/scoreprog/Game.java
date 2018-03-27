package jacobfix.scoreprog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.schedule.Season;
import jacobfix.scoreprog.util.Util;

public class Game {

    private static final String TAG = Game.class.getSimpleName();

    protected String gameId;
    protected boolean isLocked;

    public String awayAbbr;
    public String homeAbbr;
    public String awayName;
    public String homeName;
    public String awayLocale;
    public String homeLocale;
    public int awayColor;
    public int homeColor;

    public int awayScore;
    public int homeScore;

    public int quarter;
    public String clock;
    public int down;
    public int toGo;
    public String yardLine;
    public String posTeam;
    public String stadium;
    public String weather;

    private long startTime;
    private String startTimeDisplay;
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

    public static final int Q_PREGAME = -1;
    public static final int Q_HALFTIME = -2;
    public static final int Q_FINAL = -3;
    public static final int Q_FINAL_OT = -4;

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

    public void setAwayTeam(String abbr) {
        this.awayAbbr = abbr;
        this.awayLocale = NflTeamProvider.getTeamLocale(abbr);
        this.awayName = NflTeamProvider.getTeamName(abbr);
        this.awayColor = NflTeamProvider.getTeamPrimaryColor(abbr);
    }

    public void setHomeTeam(String abbr) {
        this.homeAbbr = abbr;
        this.homeLocale = NflTeamProvider.getTeamLocale(abbr);
        this.homeName = NflTeamProvider.getTeamName(abbr);
        this.homeColor = NflTeamProvider.getTeamPrimaryColor(abbr);
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
            seasonType = Util.parseSeasonType(json.getString("segment"));
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

    public String getClock() {
        return clock;
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
