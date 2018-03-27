package jacobfix.scoreprog;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import jacobfix.scoreprog.util.Util;

public class FullGame extends Game {

    private static final String TAG = FullGame.class.getSimpleName();

    private String clock;
    private int down;
    private int toGo;
    private String yardLine;
    private String posTeam;

    private String stadium;
    private String weather;

    private DriveFeed driveFeed = new DriveFeed();

    public FullGame(Game game) {
        super(game);
    }

    public void updateFromFullJson(JSONObject json) throws JSONException {
        synchronized (lock) {
            json = json.getJSONObject(gameId);
            quarter = Util.parseQuarter(json.get("qtr"));

            clock = json.getString("clock");
            down = json.getInt("down");
            toGo = json.getInt("togo");
            yardLine = json.getString("yl");
            posTeam = json.getString("posteam");

            stadium = json.getString("stadium");
            weather = json.getString("weather");

            // awayTeam.updateFromFullJson(json.getJSONObject("away"));
            // homeTeam.updateFromFullJson(json.getJSONObject("home"));

            Log.d(TAG, "Updating DriveFeed from JSON");
            driveFeed.updateFromJson(json.getJSONObject("drives"));
        }
    }

    public DriveFeed getDriveFeed() {
        return driveFeed;
    }

    public String getClock() {
        return clock;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }

    public void setClock(String clock) {
        this.clock = clock;
    }

    public void setDown(int down) {
        this.down = down;
    }

    public void setToGo(int toGo) {
        this.toGo = toGo;
    }

    public void setYardLine(String yardLine) {
        this.yardLine = yardLine;
    }

    public void setPosTeam(String posTeam) {
        this.posTeam = posTeam;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }
}
