package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

public class OriginalTeam {

    private String abbr;
    private String locale;
    private String name;
    private int primaryColor;
    private int secondaryColor;

    private int score;

    private boolean home;

    private final Object lock = new Object();

    public OriginalTeam(boolean home) {
        this.home = home;
    }

    public OriginalTeam(OriginalTeam team) {
        synchronized (team.lock) {
            this.home = team.home;
            setName(team.getAbbr());
            setScore(team.getScore());
        }
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
