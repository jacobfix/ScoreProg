package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.Season;
import jacobfix.scorepredictor.util.Util;

public class Game {

    private static final String TAG = Game.class.getSimpleName();

    private String gameId;
    private boolean isLocked;

    private Team away = new Team(false);
    private Team home = new Team(true);

    private int quarter;
    private String clock;

    private long startTime;
    private String startTimeDisplay; // Don't think this is needed

    private int season;
    private int week;
    private Schedule.Day dayOfWeek;
    private Season.SeasonType seasonType;
    private Season.WeekType weekType;

    /* Synchronization lock. */
    private final Object lock = new Object();

    private static final int Q_PREGAME = -1;
    private static final int Q_HALFTIME = -2;
    private static final int Q_FINAL = -3;
    private static final int Q_FINAL_OT = -4;

    public void updateFromScheduleJson(JSONObject json) throws JSONException {
        synchronized (lock) {
            gameId = json.getString("gid");

            away.updateFromScheduleJson(json);
            home.updateFromScheduleJson(json);

            quarter = json.getInt("quarter");

            startTime = json.getLong("start_time");
            startTimeDisplay = json.getString("start_time_display");

            week = json.getInt("week");

            dayOfWeek = Util.parseDayOfWeek(json.getString("day"));
            weekType = Util.parseWeekType(json.getString("type"), week);
            seasonType = Util.parseSeasonType(json.getString("season_segment"));
        }
    }

    public void updateFromFullJson(JSONObject json) throws JSONException {
        synchronized (lock) {

        }
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
        return isLocked();
    }

    public long getStartTime() {
        return startTime;
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

    public Team getAwayTeam() {
        return away;
    }

    public Team getHomeTeam() {
        return home;
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
