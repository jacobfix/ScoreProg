package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

public class Team {

    String abbr;
    String locale;
    String name;
    int primaryColor;
    int secondaryColor;

    int score;

    boolean home;

    private final Object lock = new Object();

    public Team(boolean home) {
        this.home = home;
    }

    public void updateFromScheduleJson(JSONObject json) throws JSONException {
        synchronized (lock) {
            setName(json.getString("name"));
            setScore(json.getInt("score"));
        }
    }

    public void setName(String abbr) {
        this.abbr = abbr;
        this.locale = NflTeamProvider.getTeamLocale(abbr);
        this.name = NflTeamProvider.getTeamName(abbr);
        this.primaryColor = NflTeamProvider.getTeamPrimaryColor(abbr);
        this.secondaryColor = NflTeamProvider.getTeamSecondaryColor(abbr);
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public int getId() {
        return 0;
    }

    public String getAbbr() {
        return abbr;
    }

    public String getLocale() {
        return locale;
    }

    public String getName() {
        return name;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public boolean isHome() {
        return home;
    }
}
