package jacobfix.scoreprog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import jacobfix.scoreprog.sync.Syncable;

public class NflTeam extends PredictableScorer implements Syncable {
    private static final String TAG = NflTeam.class.getSimpleName();

    private int mId;

    private String mAbbr;
    private String mName;
    private String mLocale;
    private String mFullName;
    private boolean mHome;

    private int mPrimaryColor = -1;
    private int mSecondaryColor = -1;

    private int mScore;
    private int[] mScoresByQuarter;

    private int mTimeouts;
    private String mTimeOfPossession;

    private HashMap<Stat, Integer> mStats;

    public NflTeam(boolean isHome) {
        mHome = isHome;
        mStats = new HashMap<Stat, Integer>();
    }

    public NflTeam(boolean isHome, String abbr) {
        this(isHome);
        setTeamName(abbr);
    }
    
    public void setTeamName(String abbr) {
        mAbbr = abbr;
        mId = abbr.hashCode();
        mLocale = NflTeamProvider.getTeamLocale(abbr);
        mName = NflTeamProvider.getTeamName(abbr);
        // mPrimaryColor = Util.softenColor(NflTeamProvider.getTeamPrimaryColor(mAbbr));
        mPrimaryColor = NflTeamProvider.getTeamPrimaryColor(mAbbr);
        // mSecondaryColor = Util.softenColor(NflTeamProvider.getTeamSecondaryColor(mAbbr));
        mSecondaryColor = NflTeamProvider.getTeamSecondaryColor(mAbbr);
    }

    public boolean isHome() {
        return mHome;
    }

    public int getId() {
        return mId;
    }

    public String getAbbr() {
        return mAbbr;
    }

    public String getLocale() {
        return mLocale;
    }

    public String getName() {
        return mName;
    }

    public int getPrimaryColor() {
        return mPrimaryColor;
    }

    public int getSecondaryColor() {
        return mSecondaryColor;
    }

    public int getScore() {
        return mScore;
    }

    public void setScore(int score) {
        mScore = score;
    }

    public int[] getScoresByQuarter() {
        return mScoresByQuarter;
    }

    public void setScoresByQuarter(int[] scoresByQuarter) {
        mScoresByQuarter = scoresByQuarter;
    }

    public int getTimeouts() {
        return mTimeouts;
    }

    public void setTimeouts(int timeouts) {
        mTimeouts = timeouts;
    }

    public int getStat(Stat stat) {
        return mStats.get(stat);
    }

    public void setStat(Stat stat, int value) {
        mStats.put(stat, value);
    }

    public String getTimeOfPossession() {
        return mTimeOfPossession;
    }

    public void setTimeOfPossession(String top) {
        mTimeOfPossession = top;
    }
    
    @Override
    public void sync(JSONObject teamJson) throws JSONException {
        setTeamName(teamJson.getString("abbr"));
        
        JSONObject scoreJson = teamJson.getJSONObject("score");
        int score = (scoreJson.get("T") != JSONObject.NULL) ? scoreJson.getInt("T") : 0;
        setScore(score);

        int[] scoresByQuarter = new int[6];
        scoresByQuarter[0] = score;
        scoresByQuarter[1] = (scoreJson.get("1") != JSONObject.NULL) ? scoreJson.getInt("1") : 0;
        scoresByQuarter[2] = (scoreJson.get("2") != JSONObject.NULL) ? scoreJson.getInt("2") : 0;
        scoresByQuarter[3] = (scoreJson.get("3") != JSONObject.NULL) ? scoreJson.getInt("3") : 0;
        scoresByQuarter[4] = (scoreJson.get("4") != JSONObject.NULL) ? scoreJson.getInt("4") : 0;
        scoresByQuarter[5] = (scoreJson.get("5") != JSONObject.NULL) ? scoreJson.getInt("5") : 0;
        setScoresByQuarter(scoresByQuarter);

        setTimeouts((teamJson.get("to") != JSONObject.NULL) ? teamJson.getInt("to") : 0);

        JSONObject teamStatsJson = teamJson.getJSONObject("stats").getJSONObject("team");
        setStat(NflTeam.Stat.FIRST_DOWNS, teamStatsJson.getInt("totfd"));
        setStat(NflTeam.Stat.TOTAL_YARDS, teamStatsJson.getInt("totyds"));
        setStat(NflTeam.Stat.PASSING_YARDS, teamStatsJson.getInt("pyds"));
        setStat(NflTeam.Stat.RUSHING_YARDS, teamStatsJson.getInt("ryds"));
        setStat(NflTeam.Stat.PENALTIES, teamStatsJson.getInt("pen"));
        setStat(NflTeam.Stat.PENALTY_YARDS, teamStatsJson.getInt("penyds"));
        setStat(NflTeam.Stat.TURNOVERS, teamStatsJson.getInt("trnovr"));
        setStat(NflTeam.Stat.PUNTS, teamStatsJson.getInt("pt"));
        setStat(NflTeam.Stat.PUNT_YARDS, teamStatsJson.getInt("ptyds"));
        setStat(NflTeam.Stat.PUNT_AVERAGE, teamStatsJson.getInt("ptavg"));

        setTimeOfPossession(teamStatsJson.getString("top"));
    }

    public enum Stat {
        FIRST_DOWNS,
        TOTAL_YARDS,
        PASSING_YARDS,
        RUSHING_YARDS,
        PENALTIES,
        PENALTY_YARDS,
        TURNOVERS,
        PUNTS,
        PUNT_YARDS,
        PUNT_AVERAGE
    }
}
