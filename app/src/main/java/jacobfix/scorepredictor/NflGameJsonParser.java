package jacobfix.scorepredictor;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

public class NflGameJsonParser {

    private static final String TAG = NflGameJsonParser.class.getSimpleName();

    public Collection<String> parseActiveGamesJson(JSONArray json) throws JSONException {
        LinkedList<String> active = new LinkedList<>();
        for (int i = 0; i < json.length(); i++) {
            active.add(json.getString(i));
        }
        Log.d(TAG, "Active games: " + active.toString());
        return active;
    }

    public void updateGameFromJson(JSONObject json, NflGame game) throws JSONException {
        json = json.getJSONObject(game.getGameId());

        updateTeamFromJson(json.getJSONObject("away"), game.getAwayTeam());
        updateTeamFromJson(json.getJSONObject("home"), game.getHomeTeam());

        Object quarterObject = json.get("qtr");
        if (quarterObject == JSONObject.NULL) {
            game.setQuarter(NflGame.QTR_PREGAME);
            game.setPregame(true);
            game.setFinal(false);
        } else if (quarterObject instanceof String) {
            if (quarterObject.equals("Pregame")) {
                game.setQuarter(NflGame.QTR_PREGAME);
                game.setPregame(true);
                game.setFinal(false);
            } else if (quarterObject.equals("Final") || quarterObject.equals("final overtime")) {
                game.setQuarter(NflGame.QTR_FINAL);
                game.setPregame(false);
                game.setFinal(true);
            } else if (quarterObject.equals("Halftime")) {
                game.setQuarter(NflGame.QTR_HALFTIME);
                game.setPregame(false);
                game.setFinal(false);
            }
        } else {
            game.setQuarter(json.getInt("qtr"));
            game.setPregame(false);
            game.setFinal(false);
        }

        game.setDown((json.get("down") != JSONObject.NULL) ? json.getInt("down") : 0);
        game.setToGo((json.get("togo") != JSONObject.NULL) ? json.getInt("togo") : 0);
        game.setClock((json.get("clock") != JSONObject.NULL) ? json.getString("clock") : null);
        game.setYardLine((json.get("yl") != JSONObject.NULL) ? json.getString("yl") : null);

        Object posTeamAbbrObject = json.get("posteam");
        if (posTeamAbbrObject == JSONObject.NULL) {
            game.setPosTeam(null);
        } else {
            String posTeamAbbr = json.getString("posteam");
            if (posTeamAbbr.equals(game.getAwayTeam().getAbbr())) {
                game.setPosTeam(game.getAwayTeam());
            } else {
                game.setPosTeam(game.getHomeTeam());
            }
        }

        game.setRedZone((json.get("redzone") != JSONObject.NULL) ? json.getBoolean("redzone") : false);
        game.setStadium((json.get("stadium") != JSONObject.NULL) ? json.getString("stadium") : null);
        // game.setTv((json.getJSONObject("media").get("tv") != JSONObject.NULL) ? json.getJSONObject("media").getString("tv") : null);
        // Weather?
    }

    private void updateTeamFromJson(JSONObject json, NflTeam team) throws JSONException {
        team.setTeamName(json.getString("abbr"));

        JSONObject scoreJson = json.getJSONObject("score");
        int score = (scoreJson.get("T") != JSONObject.NULL) ? scoreJson.getInt("T") : 0;
        team.setScore(score);
        
        int[] scoresByQuarter = new int[6];
        scoresByQuarter[0] = score;
        scoresByQuarter[1] = (scoreJson.get("1") != JSONObject.NULL) ? scoreJson.getInt("1") : 0;
        scoresByQuarter[2] = (scoreJson.get("2") != JSONObject.NULL) ? scoreJson.getInt("2") : 0;
        scoresByQuarter[3] = (scoreJson.get("3") != JSONObject.NULL) ? scoreJson.getInt("3") : 0;
        scoresByQuarter[4] = (scoreJson.get("4") != JSONObject.NULL) ? scoreJson.getInt("4") : 0;
        scoresByQuarter[5] = (scoreJson.get("5") != JSONObject.NULL) ? scoreJson.getInt("5") : 0;
        team.setScoresByQuarter(scoresByQuarter);

        team.setTimeouts((json.get("to") != JSONObject.NULL) ? json.getInt("to") : 0);

        JSONObject teamStatsJson = json.getJSONObject("stats").getJSONObject("team");
        team.setStat(NflTeam.Stat.FIRST_DOWNS, teamStatsJson.getInt("totfd"));
        team.setStat(NflTeam.Stat.TOTAL_YARDS, teamStatsJson.getInt("totyds"));
        team.setStat(NflTeam.Stat.PASSING_YARDS, teamStatsJson.getInt("pyds"));
        team.setStat(NflTeam.Stat.RUSHING_YARDS, teamStatsJson.getInt("ryds"));
        team.setStat(NflTeam.Stat.PENALTIES, teamStatsJson.getInt("pen"));
        team.setStat(NflTeam.Stat.PENALTY_YARDS, teamStatsJson.getInt("penyds"));
        team.setStat(NflTeam.Stat.TURNOVERS, teamStatsJson.getInt("trnovr"));
        team.setStat(NflTeam.Stat.PUNTS, teamStatsJson.getInt("pt"));
        team.setStat(NflTeam.Stat.PUNT_YARDS, teamStatsJson.getInt("ptyds"));
        team.setStat(NflTeam.Stat.PUNT_AVERAGE, teamStatsJson.getInt("ptavg"));

        team.setTimeOfPossession(teamStatsJson.getString("top"));
    }
}
