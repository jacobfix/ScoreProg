package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.Season;
import jacobfix.scorepredictor.util.Util;

public class AtomicGame {

    private static final String TAG = AtomicGame.class.getSimpleName();

    private String gid;
    private boolean locked;

    private OriginalTeam away = new OriginalTeam(false);
    private OriginalTeam home = new OriginalTeam(true);

    private String awayAbbr;
    private String homeAbbr;

    private String awayLocale;
    private String homeLocale;
    private String awayName;
    private String homeName;

    private int awayScore;
    private int homeScore;

    private int awayColor;
    private int homeColor;

    private int quarter;
    private boolean isPregame;
    private boolean isFinal;

    private long startTime;

    private String startTimeDisplay;
    private Schedule.Day dayOfWeek;
    private int season;
    private int week;
    private Season.SeasonType seasonType;
    public Season.WeekType weekType;

    private final Object lock = new Object();

    public static final int Q_PREGAME = -1;
    public static final int Q_HALFTIME = -2;
    public static final int Q_FINAL = -3;
    public static final int Q_FINAL_OT = -4;

    public void updateFromScheduleJson(JSONObject json) throws JSONException {
        synchronized (lock) {
            gid = json.getString("gid");

            away.updateFromScheduleJson(json.getJSONObject("away"));
            home.updateFromScheduleJson(json.getJSONObject("home"));

            quarter = json.getInt("quarter");

            startTime = json.getLong("start_time");
            startTimeDisplay = json.getString("start_time_display");

            week = json.getInt("week");

            dayOfWeek = Util.parseDayOfWeek(json.getString("day"));
            weekType = Util.parseWeekType(json.getString("type"), week);
            seasonType = Util.parseSeasonType(json.getString("season_segment"));
        }
    }

    public void sync(JSONObject json) throws JSONException {
        synchronized (this) {
            gid = json.getString("gid");
            awayAbbr = json.getString("away");
            homeAbbr = json.getString("home");

            away.setName(awayAbbr);
            home.setName(homeAbbr);

            /*
            awayLocale = NflTeamProvider.getTeamLocale(awayAbbr);
            homeLocale = NflTeamProvider.getTeamLocale(homeAbbr);
            awayName = NflTeamProvider.getTeamName(awayAbbr);
            homeName = NflTeamProvider.getTeamName(homeAbbr);
            awayColor = NflTeamProvider.getTeamPrimaryColor(awayAbbr);
            homeColor = NflTeamProvider.getTeamPrimaryColor(homeAbbr);
            */

            awayScore = json.getInt("away_score");
            homeScore = json.getInt("home_score");

            away.setScore(awayScore);
            home.setScore(homeScore);

            // away.score = awayScore;
            // home.score = homeScore;

            quarter = json.getInt("quarter");

            startTime = json.getLong("start_time");
            startTimeDisplay = json.getString("start_time_display");
            dayOfWeek = Util.parseDayOfWeek(json.getString("day"));
            week = json.getInt("week");
            weekType = Util.parseWeekType(json.getString("type"), week);
            seasonType = Util.parseSeasonType(json.getString("season_segment"));

            /*
            switch (quarter) {
                case Q_PREGAME:
                    isPregame = true;
                    isFinal = false;
                    break;
                case Q_FINAL:
                    isPregame = false;
                    isFinal = true;
                    break;
                case Q_FINAL_OT:
                    isPregame = false;
                    isFinal = true;
                    break;
                case Q_HALFTIME:
                    isPregame = false;
                    isFinal = false;
                    break;
                default:
                    isPregame = false;
                    isFinal = false;
            }
            */
        }
    }

    public void lock() {
        locked = true;
    }

    public boolean isPregame() {
        return quarter == Q_PREGAME;
    }

    public boolean isFinal() {
        return quarter == Q_FINAL || quarter == Q_FINAL_OT;
    }

    public boolean inProgress() {
        return !isPregame() && !isFinal();
    }

    public String getId() {
        return gid;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getStartTimeDisplay() {
        return startTimeDisplay;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getQuarter() {
        return quarter;
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

    public int getAwayScore() {
        return awayScore;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayColor() {
        return awayColor;
    }

    public int getHomeColor() {
        return homeColor;
    }

    public OriginalTeam getAwayTeam() {
        return away;
    }

    public OriginalTeam getHomeTeam() {
        return home;
    }

    public Season.WeekType getWeekType() {
        return weekType;
    }

    public int getWeek() {
        return week;
    }

    public Schedule.Day getDayOfWeek() {
        return dayOfWeek;
    }
}
