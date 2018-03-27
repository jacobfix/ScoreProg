package jacobfix.scoreprog.schedule;

import org.json.JSONException;
import org.json.JSONObject;

public class ScheduledGame {

    public String gid;
    public String awayAbbr;
    public String homeAbbr;
    public long startTime;
    public String startTimeDisplay;
    public String meridiem;
    public Schedule.Day dayOfWeek;
    public int season;
    public int week;
    public int seasonType;
    public Season.WeekType weekType;

    public ScheduledGame() {

    }

    public ScheduledGame(ScheduledGame other) {

    }

    public void sync(JSONObject json) throws JSONException {
        synchronized (this) {
            gid = json.getString("gid");
        }
    }
}