package jacobfix.scorepredictor;

import java.util.HashMap;

import jacobfix.scorepredictor.util.Util;

public class NflTeam extends PredictableScorer {

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
        mPrimaryColor = Util.softenColor(NflTeamProvider.getTeamPrimaryColor(mAbbr));
        mSecondaryColor = Util.softenColor(NflTeamProvider.getTeamSecondaryColor(mAbbr));
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
