package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Play {

    private int mQuarter;
    private int mDown;
    private String mTime;
    private String mYardLine;
    private int mYardsToGo;
    private int mYardsNet;
    private String mPosTeamAbbr;
    private int mPosTeamId;
    private String mDescription;
    private String mNote;
    private SequenceItem[] mSequence;

    private int mJsonHash;

    public int getQuarter() {
        return mQuarter;
    }

    public void setQuarter(int qtr) {
        mQuarter = qtr;
    }

    public int getDown() {
        return mDown;
    }

    public void setDown(int down) {
        mDown = down;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getYardLine() {
        return mYardLine;
    }

    public void setYardLine(String yl) {
        mYardLine = yl;
    }

    public int getYardsToGo() {
        return mYardsToGo;
    }

    public void setYardsToGo(int toGo) {
        mYardsToGo = toGo;
    }

    public int getYardsNet() {
        return mYardsNet;
    }

    public void setYardsNet(int yardsNet) {
        mYardsNet = yardsNet;
    }

    public String getPosTeamAbbr() {
        return mPosTeamAbbr;
    }

    public void setPosTeamAbbr(String abbr) {
        mPosTeamAbbr = abbr;
        mPosTeamId = abbr.hashCode();
    }

    public int getPosTeamId() {
        return mPosTeamId;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String desc) {
        mDescription = desc;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public int getJsonHash() {
        return mJsonHash;
    }

    public void setJsonHash(int hash) {
        mJsonHash = hash;
    }

    public SequenceItem[] getPlaySequence() {
        return mSequence;
    }

    public void setPlaySequence(SequenceItem[] sequence) {
        mSequence = sequence;
    }

    public SequenceItem getSequenceItem(int index) {
        return mSequence[index];
    }

    public int numSequenceItems() {
        return mSequence.length;
    }

    public void addSequenceItem(int index, SequenceItem item) {
        mSequence[index] = item;
    }

    class PlayBundle {
    }

    static class SequenceItem {
        private int mSeqno;
        private String mTeamAbbr;
        private String mPlayerName;
        private int mStatId;
        private int mYards;

        public int getSequenceNumber() {
            return mSeqno;
        }

        public void setSequenceNumber(int seqno) {
            mSeqno = seqno;
        }

        public String getTeamAbbr() {
            return mTeamAbbr;
        }

        public void setTeamAbbr(String teamAbbr) {
            mTeamAbbr = teamAbbr;
        }

        public String getPlayerName() {
            return mPlayerName;
        }

        public void setPlayerName(String playerName) {
            mPlayerName = playerName;
        }

        public int getStatId() {
            return mStatId;
        }

        public void setStatId(int statId) {
            mStatId = statId;
        }

        public int getYards() {
            return mYards;
        }

        public void setYards(int yards) {
            mYards = yards;
        }

        public boolean hasPlayer() {
            return !mPlayerName.equals("");
        }
    }
}
