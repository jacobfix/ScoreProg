package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.ScheduledGame;
import jacobfix.scorepredictor.schedule.Season;
import jacobfix.scorepredictor.sync.Syncable;

public class NflGame {

    private static final String TAG = NflGame.class.getSimpleName();

    private String mGameId;

    /* Schedule. */
    private boolean locked;
    private long startTime;
    private String startTimeDisplay;
    private String meridiem;
    private Schedule.Day dayOfWeek;
    private int season;
    private int week;
    private int seasonType;
    private Season.WeekType weekType;

    // TODO: Boolean variable to indicate whether a game has downloaded all the extra gamecenter data?
    /* Game state. */
    private int quarter;
    private int down;
    private int toGo;
    private String yardLine;
    private String clock;
    private boolean redZone;
    private String stadium;
    private String tv;
    private boolean isPregame;
    private boolean isFinal;

    /* Teams. */
    private NflTeam awayTeam = new NflTeam(false);
    private NflTeam homeTeam = new NflTeam(true);
    private NflTeam posTeam;

    private DriveFeed mDriveFeed = new DriveFeed();

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd hh:mm aa");

    public static final int QTR_FIRST = 1;
    public static final int QTR_SECOND = 2;
    public static final int QTR_THIRD = 3;
    public static final int QTR_FOURTH = 4;
    public static final int QTR_OVERTIME = 5;
    public static final int QTR_PREGAME = 0xfff0;
    public static final int QTR_HALFTIME = 0xfff1;
    public static final int QTR_FINAL = 0xfff2;
    public static final int QTR_FINAL_OVERTIME = 0xfff3;

    public NflGame() {
        awayTeam = new NflTeam(false);
        homeTeam = new NflTeam(true);
        mDriveFeed = new DriveFeed();
    }

    public NflGame(ScheduledGame game) {
        syncScheduleDetails(game);
    }

    public NflGame(String gameId) {
        mGameId = gameId;
        awayTeam = new NflTeam(false);
        homeTeam = new NflTeam(true);
        mDriveFeed = new DriveFeed();
    }

    public String getGameId() {
        return mGameId;
    }

    public long getStartTime() {
        /* Scheduled start time in milliseconds since epoch. */
        return startTime;
    }

    public int getSeason() {
        return season;
    }

    public int getWeek() {
        return week;
    }

    public Season.WeekType getWeekType() {
        return weekType;
    }

    public boolean isPredicted() {
        return false;
    }

    public NflTeam getPredictedWinner() {
        return null;
    }

    public boolean wasPredictedCorrectly() {
        return false;
    }

    private NflTeam getLeadingTeam() {
        if (awayTeam.getScore() < homeTeam.getScore()) {
            return homeTeam;
        } else if (awayTeam.getScore() == homeTeam.getScore()) {
            return null;
        } else {
            return awayTeam;
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public NflTeam getAwayTeam() {
        return this.awayTeam;
    }

    public void setAwayTeam(NflTeam team) {
        this.awayTeam = team;
    }

    public void setHomeTeam(NflTeam team) {
        this.homeTeam = team;
    }

    public NflTeam getHomeTeam() {
        return this.homeTeam;
    }

    public boolean isPregame() {
        return this.isPregame;
    }

    public void setPregame(boolean isPregame) {
        this.isPregame = isPregame;
    }

    public boolean isFinal() {
        return this.isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean inProgress() {
        return !this.isFinal && !this.isPregame;
    }

    public int getQuarter() {
        return this.quarter;
    }

    public void setQuarter(int qtr) {
        this.quarter = qtr;
    }

    public int getDown() {
        return this.down;
    }

    public void setDown(int down) {
        this.down = down;
    }

    public int getToGo() {
        return this.toGo;
    }

    public void setToGo(int toGo) {
        this.toGo = toGo;
    }

    public String getClock() {
        return this.clock;
    }

    public void setClock(String clock) {
        this.clock = clock;
    }

    public String getYardLine() {
        return this.yardLine;
    }

    public void setYardLine(String yl) {
        this.yardLine = yl;
    }

    public NflTeam getPosTeam() {
        return this.posTeam;
    }

    public void setPosTeam(NflTeam team) {
        /*
        if (team != awayTeam && team != homeTeam) {
            throw new AssertionError("Team with possession does not belong to game");
        }
        */
        this.posTeam = team;
    }

    public boolean inRedZone() {
        return this.redZone;
    }

    public void setRedZone(boolean redZone) {
        this.redZone = redZone;
    }

    public String getStadium() {
        return this.stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public String getTv() {
        return this.tv;
    }

    public void setTv(String tv) {
        this.tv = tv;
    }

    public DriveFeed getDriveFeed() {
        return mDriveFeed;
    }

    public void syncScheduleDetails(ScheduledGame scheduledGame) {
        synchronized (this) {
            mGameId = scheduledGame.gid;
            awayTeam.setTeamName(scheduledGame.awayAbbr);
            homeTeam.setTeamName(scheduledGame.homeAbbr);
            startTime = scheduledGame.startTime;
            startTimeDisplay = scheduledGame.startTimeDisplay;
            meridiem = scheduledGame.meridiem;
            dayOfWeek = scheduledGame.dayOfWeek;
            season = scheduledGame.season;
            week = scheduledGame.week;
            seasonType = scheduledGame.seasonType;
            weekType = scheduledGame.weekType;

            isPregame = true;
        }
    }

    public void syncFullDetails(JSONObject gameJson) throws JSONException {
        synchronized (this) {
            awayTeam.sync(gameJson.getJSONObject("away"));
            homeTeam.sync(gameJson.getJSONObject("home"));

            Object quarterObj = gameJson.get("qtr");
            if (quarterObj == JSONObject.NULL) {
                quarter = NflGame.QTR_PREGAME;
                setPregame(true);
                setFinal(false);
            } else if (quarterObj instanceof String) {
                if (quarterObj.equals("Pregame")) {
                    setQuarter(NflGame.QTR_PREGAME);
                    setPregame(true);
                    setFinal(false);
                } else if (quarterObj.equals("Final") || quarterObj.equals("final overtime")) {
                    setQuarter(NflGame.QTR_FINAL);
                    setPregame(false);
                    setFinal(true);
                } else if (quarterObj.equals("Halftime")) {
                    setQuarter(NflGame.QTR_HALFTIME);
                    setPregame(false);
                    setFinal(false);
                }
            } else {
                setQuarter(gameJson.getInt("qtr"));
                setPregame(false);
                setFinal(false);
            }

            setDown((gameJson.get("down") != JSONObject.NULL) ? gameJson.getInt("down") : 0);
            setToGo((gameJson.get("togo") != JSONObject.NULL) ? gameJson.getInt("togo") : 0);
            setClock((gameJson.get("clock") != JSONObject.NULL) ? gameJson.getString("clock") : null);
            setYardLine((gameJson.get("yl") != JSONObject.NULL) ? gameJson.getString("yl") : null);

            Object posTeamAbbrObject = gameJson.get("posteam");
            if (posTeamAbbrObject == JSONObject.NULL) {
                setPosTeam(null);
            } else {
                String posTeamAbbr = gameJson.getString("posteam");
                if (posTeamAbbr.equals(getAwayTeam().getAbbr())) {
                    setPosTeam(getAwayTeam());
                } else {
                    setPosTeam(getHomeTeam());
                }
            }

            setRedZone((gameJson.get("redzone") != JSONObject.NULL) ? gameJson.getBoolean("redzone") : false);
            setStadium((gameJson.get("stadium") != JSONObject.NULL) ? gameJson.getString("stadium") : null);
        }
    }

    private Date getUtcStartTime(String gid, String startTime) {
        String dateString = gid.substring(0, 9) + " " + startTime;
        try {
            dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
        return null;
    }

    public String getStartTimeDisplay() {
        return startTimeDisplay;
    }

    public String getMeridiem() {
        return meridiem;
    }
}
