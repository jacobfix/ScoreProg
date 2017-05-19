package jacobfix.scorepredictor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;

public class OriginalNflGameJsonParser {

    private static final String TAG = OriginalNflGameJsonParser.class.getSimpleName();

    public static String getAwayTeamAbbr(JSONObject simpleGameJson) throws JSONException {
        return simpleGameJson.getJSONObject("away").getString("abbr");
    }

    public static String getHomeTeamAbbr(JSONObject simpleGameJson) throws JSONException {
        return simpleGameJson.getJSONObject("home").getString("abbr");
    }

    public static void updateGameFromSimpleJson(NflGame game, JSONObject simpleJson) throws JSONException {
        updateTeamFromSimpleJson(game.getAwayTeam(), simpleJson.getJSONObject("away"));
        updateTeamFromSimpleJson(game.getHomeTeam(), simpleJson.getJSONObject("home"));

        Object quarterObject = simpleJson.get("qtr");
        if (quarterObject == JSONObject.NULL) {
            game.setQuarter(NflGame.QTR_PREGAME);
            game.setPregame(true);
            game.setFinal(false);
        } else if (quarterObject instanceof String) {
            if (quarterObject.equals("Pregame")) {
                game.setQuarter(0);
                game.setPregame(true);
                game.setFinal(false);
            } else if (quarterObject.equals("Final") || quarterObject.equals("final overtime")) {
                game.setQuarter(NflGame.QTR_PREGAME);
                game.setPregame(false);
                game.setFinal(true);
            } else if (quarterObject.equals("Halftime")) {
                game.setQuarter(NflGame.QTR_HALFTIME);
                game.setPregame(false);
                game.setFinal(false);
            }
        } else {
            game.setQuarter(simpleJson.getInt("qtr"));
            game.setPregame(false);
            game.setFinal(false);
        }

        game.setDown((simpleJson.get("down") != JSONObject.NULL) ? simpleJson.getInt("down") : 0);
        game.setToGo((simpleJson.get("togo") != JSONObject.NULL) ? simpleJson.getInt("togo") : 0);
        game.setClock((simpleJson.get("clock") != JSONObject.NULL) ? simpleJson.getString("clock") : null);
        game.setYardLine((simpleJson.get("yl") != JSONObject.NULL) ? simpleJson.getString("yl") : null);

        Object posTeamAbbrObject = simpleJson.get("posteam");
        if (posTeamAbbrObject == JSONObject.NULL) {
            game.setPosTeam(null);
        } else {
            String posTeamAbbr = simpleJson.getString("posteam");
            if (posTeamAbbr.equals(game.getAwayTeam().getAbbr())) {
                game.setPosTeam(game.getAwayTeam());
            } else {
                game.setPosTeam(game.getHomeTeam());
            }
        }

        game.setRedZone((simpleJson.get("redzone") != JSONObject.NULL) ? simpleJson.getBoolean("redzone") : false);
        game.setStadium((simpleJson.get("stadium") != JSONObject.NULL) ? simpleJson.getString("stadium") : null);
        game.setTv((simpleJson.getJSONObject("media").get("tv") != JSONObject.NULL) ? simpleJson.getJSONObject("media").getString("tv") : null);
    }

    public static void updateGameFromDetailedJson(NflGame game, JSONObject detailedJson) throws JSONException {
        updateTeamFromDetailedJson(game.getAwayTeam(), detailedJson.getJSONObject("away"));
        updateTeamFromDetailedJson(game.getHomeTeam(), detailedJson.getJSONObject("home"));
        updateGameDriveFeedFromJson(game.getDriveFeed(), detailedJson.getJSONObject("drives"));
    }

    public static void updateTeamFromSimpleJson(NflTeam team, JSONObject simpleTeamJson) throws JSONException {
        team.setTeamName(simpleTeamJson.getString("abbr"));

        JSONObject teamScoreJSON = simpleTeamJson.getJSONObject("score");
        int score = (teamScoreJSON.get("T") != JSONObject.NULL) ? teamScoreJSON.getInt("T") : 0;
        team.setScore(score);

        int[] scoresByQuarter = new int[6];
        scoresByQuarter[0] = score;
        scoresByQuarter[1] = (teamScoreJSON.get("1") != JSONObject.NULL) ? teamScoreJSON.getInt("1") : 0;
        scoresByQuarter[2] = (teamScoreJSON.get("2") != JSONObject.NULL) ? teamScoreJSON.getInt("2") : 0;
        scoresByQuarter[3] = (teamScoreJSON.get("3") != JSONObject.NULL) ? teamScoreJSON.getInt("3") : 0;
        scoresByQuarter[4] = (teamScoreJSON.get("4") != JSONObject.NULL) ? teamScoreJSON.getInt("4") : 0;
        scoresByQuarter[5] = (teamScoreJSON.get("5") != JSONObject.NULL) ? teamScoreJSON.getInt("5") : 0;
        team.setScoresByQuarter(scoresByQuarter);

        team.setTimeouts((simpleTeamJson.get("to") != JSONObject.NULL) ? simpleTeamJson.getInt("to") : 0);
    }

    public static void updateTeamFromDetailedJson(NflTeam team, JSONObject detailedTeamJson) throws JSONException {
        JSONObject teamStatsJSON = detailedTeamJson.getJSONObject("stats").getJSONObject("team");
        team.setStat(NflTeam.Stat.FIRST_DOWNS, teamStatsJSON.getInt("totfd"));
        team.setStat(NflTeam.Stat.TOTAL_YARDS, teamStatsJSON.getInt("totyds"));
        team.setStat(NflTeam.Stat.PASSING_YARDS, teamStatsJSON.getInt("pyds"));
        team.setStat(NflTeam.Stat.RUSHING_YARDS, teamStatsJSON.getInt("ryds"));
        team.setStat(NflTeam.Stat.PENALTIES, teamStatsJSON.getInt("pen"));
        team.setStat(NflTeam.Stat.PENALTY_YARDS, teamStatsJSON.getInt("penyds"));
        team.setStat(NflTeam.Stat.TURNOVERS, teamStatsJSON.getInt("trnovr"));
        team.setStat(NflTeam.Stat.PUNTS, teamStatsJSON.getInt("pt"));
        team.setStat(NflTeam.Stat.PUNT_YARDS, teamStatsJSON.getInt("ptyds"));
        team.setStat(NflTeam.Stat.PUNT_AVERAGE, teamStatsJSON.getInt("ptavg"));

        team.setTimeOfPossession(teamStatsJSON.getString("top"));
    }

    public static void updateGameDriveFeedFromJson(DriveFeed driveFeed, JSONObject drivesJson) throws JSONException {
        Iterator<String> iter = drivesJson.keys();
        int driveCounter = 0;
        while (iter.hasNext()) {
            String driveNum = iter.next();
            driveCounter++;

            if (driveNum.equals("crntdrv")) {
                continue;
            }

            if (driveCounter <= driveFeed.numDrives()) {
                // if (driveFeed.getDrive(driveCounter - 1).getJsonHash() != drivesJson.getJSONObject(driveNum).hashCode()) {
                    updateDriveFromJson(driveFeed.getDrive(driveCounter - 1), drivesJson.getJSONObject(driveNum));
                // }
            } else {
                Drive drive = new Drive();
                updateDriveFromJson(drive, drivesJson.getJSONObject(driveNum));
                driveFeed.addDrive(drive);
            }
        }
    }

    public static void updateDriveFromJson(Drive drive, JSONObject driveJson) throws JSONException {
        drive.setJsonHash(driveJson.hashCode());
        drive.setQuarter(driveJson.getInt("qtr"));
        drive.setRedZone(driveJson.getBoolean("redzone"));

        JSONObject playsJson = driveJson.getJSONObject("plays");
        Iterator<String> iter = playsJson.keys();
        int playCounter = 0;
        while (iter.hasNext()) {
            String playId = iter.next();
            playCounter++;

            if (playCounter <= drive.numPlays()) {
                // if (drive.getPlay(playCounter - 1).getJsonHash() != playsJson.getJSONObject(playId).hashCode()) {
                    // Log.d(TAG, "Json hashes don't match");
                    updatePlayFromJson(drive.getPlay(playCounter - 1), playsJson.getJSONObject(playId));
                    // Log.d(TAG, "Json hashes match");
                // }
            } else {
                Play play = new Play();
                updatePlayFromJson(play, playsJson.getJSONObject(playId));
                drive.addPlay(play);
            }
        }
    }

    public static void updatePlayFromJson(Play play, JSONObject playJson) throws JSONException {
        play.setJsonHash(playJson.hashCode());
        play.setQuarter(playJson.getInt("qtr"));
        play.setDown(playJson.getInt("down"));
        play.setTime(playJson.getString("time"));
        play.setYardLine(playJson.getString("yrdln"));
        play.setYardsToGo(playJson.getInt("ydstogo"));
        play.setYardsNet(playJson.getInt("ydsnet"));
        play.setPosTeamAbbr(playJson.getString("posteam"));
        play.setDescription(playJson.getString("desc"));
        play.setNote(playJson.getString("note"));

        JSONObject playSequenceJson = playJson.getJSONObject("players");
        LinkedList<Play.SequenceItem> unorderedPlaySequence = new LinkedList<Play.SequenceItem>();
        Iterator<String> iter = playSequenceJson.keys();
        while (iter.hasNext()) {
            String playerId = iter.next();
            JSONArray playerRoleJson = playSequenceJson.getJSONArray(playerId);
            for (int i = 0; i < playerRoleJson.length(); i++) {
                Play.SequenceItem item = new Play.SequenceItem();
                updatePlaySequenceItemFromJson(item, playerRoleJson.getJSONObject(i));
                unorderedPlaySequence.add(item);
            }
        }
        Play.SequenceItem[] playSequence = new Play.SequenceItem[unorderedPlaySequence.size()];
        for (Play.SequenceItem item : unorderedPlaySequence) {
            playSequence[item.getSequenceNumber() - 1] = item;
        }
        play.setPlaySequence(playSequence);
    }

    public static void updatePlaySequenceItemFromJson(Play.SequenceItem item, JSONObject playSequenceJson) throws JSONException {
        item.setSequenceNumber(playSequenceJson.getInt("sequence"));
        item.setTeamAbbr(playSequenceJson.getString("clubcode"));
        item.setPlayerName(playSequenceJson.getString("playerName"));
        item.setStatId(playSequenceJson.getInt("statId"));
        item.setYards(playSequenceJson.getInt("yards"));
    }
}
