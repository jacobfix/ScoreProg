package jacobfix.scoreprog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import jacobfix.scoreprog.util.Util;

public class Drive {

    private ArrayList<Play> plays = new ArrayList<>();

    private String posTeam;

    private int quarter;
    private boolean redZone;

    private String result;
    private String posTime;
    private int yardsGained;

    private int startQuarter, endQuarter;
    private String startClock, endClock;
    private String startYardLine, endYardLine;
    private String startTeam, endTeam;

    private int lastJsonHash = 0;

    private final Object lock = new Object();

    public void updateFromJson(JSONObject json) throws JSONException {
        int jsonHash = json.toString().hashCode();
        if (jsonHash == lastJsonHash)
            return;

        synchronized (lock) {
            quarter = Util.parseQuarter(json.get("qtr"));
            redZone = json.getBoolean("redzone");
            result = json.getString("result");
            posTeam = json.getString("posteam");
            posTime = json.getString("postime");
            yardsGained = json.getInt("ydsgained");

            JSONObject startJson = json.getJSONObject("start");
            startQuarter = Util.parseQuarter(startJson.get("qtr"));
            startClock = startJson.getString("time");
            startTeam = startJson.getString("team");
            startYardLine = startJson.getString("yrdln");

            JSONObject endJson = json.getJSONObject("end");
            endQuarter = Util.parseQuarter(endJson.get("qtr"));
            endClock = endJson.getString("time");
            endTeam = endJson.getString("team");
            endYardLine = endJson.getString("yrdln");

            JSONObject playsJson = json.getJSONObject("plays");

            LinkedList<String> playIds = new LinkedList<>();
            Iterator<String> playIterator = playsJson.keys();
            while (playIterator.hasNext()) {
                playIds.add(playIterator.next());
            }
            Collections.sort(playIds);

            JSONObject playJson;
            for (int i = 0; i < playIds.size(); i++) {
                String playId = playIds.get(i);
                playJson = playsJson.getJSONObject(playId);

                if (i < plays.size()) {
                    plays.get(i).updateFromJson(playJson);
                } else {
                    Play play = new Play();
                    play.updateFromJson(playJson);
                    plays.add(i, play);
                }
            }

            lastJsonHash = jsonHash;
        }
    }

    public ArrayList<Play> plays() {
        return plays;
    }

    public int numPlays() {
        return plays.size();
    }

    public Play getPlay(int index) {
        return plays.get(index);
    }

    public void addPlay(int index, Play play) {
        plays.add(index, play);
    }

    public String getPosTeam() {
        return posTeam;
    }

    public int getQuarter() {
        return quarter;
    }

    public boolean inRedZone() {
        return redZone;
    }

    public String getResult() {
        return result;
    }

    public String getTimeOfPossession() {
        return posTime;
    }

    public int getYardsGained() {
        return yardsGained;
    }

    public void setRedZone(boolean redZone) {
        this.redZone = redZone;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setPosTime(String posTime) {
        this.posTime = posTime;
    }

    public void setPosTeam(String posTeam) {
        this.posTeam = posTeam;
    }

    public void setYardsGained(int yardsGained) {
        this.yardsGained = yardsGained;
    }

    public void setStartQuarter(int startQuarter) {
        this.startQuarter = startQuarter;
    }

    public void setEndQuarter(int endQuarter) {
        this.endQuarter = endQuarter;
    }

    public void setStartClock(String startClock) {
        this.startClock = startClock;
    }

    public void setEndClock(String endClock) {
        this.endClock = endClock;
    }

    public void setStartYardLine(String startYardLine) {
        this.startYardLine = startYardLine;
    }

    public void setStartTeam(String startTeam) {
        this.startTeam = startTeam;
    }

    public void setEndYardLine(String endYardLine) {
        this.endYardLine = endYardLine;
    }

    public void setEndTeam(String endTeam) {
        this.endTeam = endTeam;
    }

    public int getStartQuarter() {
        return startQuarter;
    }

    public int getEndQuarter() {
        return endQuarter;
    }

    public String getStartClock() {
        return startClock;
    }

    public String getEndClock() {
        return endClock;
    }

    public String getStartYardLine() {
        return startYardLine;
    }

    public String getEndYardLine() {
        return endYardLine;
    }

    public String getStartTeam() {
        return startTeam;
    }

    public String getEndTeam() {
        return endTeam;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public Object acquireLock() {
        return lock;
    }
}
