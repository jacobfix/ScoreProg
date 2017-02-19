package jacobfix.scorepredictor;

import java.util.ArrayList;
import java.util.HashMap;

public class Drive {

    private ArrayList<Play> mPlayFeed;

    private String mPosTeamAbbr;
    private int mPosTeamId;

    private int mQuarter;
    private boolean mRedZone;

    private String mResult;
    private String mPossessionTime;

    private int mStartQuarter, mEndQuarter;
    private String mStartTime, mEndTime;
    private String mStartYardLine, mEndYardLine;
    private String mStartTeam, mEndTeam;

    private HashMap<Stat, Integer> mStats;

    private int mJsonHash;

    public Drive() {
        mPlayFeed = new ArrayList<Play>();
        mStats = new HashMap<Stat, Integer>();
    }

    public void addPlay(Play play) {
        mPlayFeed.add(play);
    }

    public Play getPlay(int index) {
        return mPlayFeed.get(index);
    }

    public int numPlays() {
        return mPlayFeed.size();
    }

    public ArrayList<Play> getPlays() {
        return mPlayFeed;
    }

    public int getQuarter() {
        return mQuarter;
    }

    public void setQuarter(int qtr) {
        mQuarter = qtr;
    }

    public boolean getRedZone() {
        return mRedZone;
    }

    public void setRedZone(boolean redZone) {
        mRedZone = redZone;
    }

    public int getJsonHash() {
        return mJsonHash;
    }

    public void setJsonHash(int hash) {
        mJsonHash = hash;
    }

    public enum Stat {
        FIRST_DOWNS,
        PENALTY_YARDS,
        YARDS_GAINED,
        NUM_PLAYS
    }
}
