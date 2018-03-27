package jacobfix.scoreprog.server;

import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import jacobfix.scoreprog.Drive;
import jacobfix.scoreprog.DriveFeed;
import jacobfix.scoreprog.FindUsersActivity;
import jacobfix.scoreprog.Friends;
import jacobfix.scoreprog.FullGame;
import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.Login;
import jacobfix.scoreprog.NflGame;
import jacobfix.scoreprog.NflTeam;
import jacobfix.scoreprog.Play;
import jacobfix.scoreprog.Prediction;
import jacobfix.scoreprog.RelationDetails;
import jacobfix.scoreprog.users.User;
import jacobfix.scoreprog.users.UserDetails;
import jacobfix.scoreprog.util.Util;

public class JsonParser {

    /* This vector must match the errors vector in the server script. */
    /*
    public static final int[] LOGIN_ERROR_CODES = new int[]{
            LoginTask.LOGIN_ERROR_NONE,
            LoginTask.LOGIN_ERROR_INSUFFICIENT_PARAMS,
            LoginTask.LOGIN_ERROR_EMAIL_NO_MATCH,
            LoginTask.LOGIN_ERROR_USERNAME_NO_MATCH,
            LoginTask.LOGIN_ERROR_INVALID_PASSWORD,
            LoginTask.LOGIN_ERROR_DATABASE_FAILURE,
    };
    */

    public static final int FRIEND_NONE = 0;
    public static final int FRIEND_CONFIRMED = 1;
    public static final int FRIEND_PENDING_SENT = 2;
    public static final int FRIEND_PENDING_RECEIVED = 3;
    public static final int FRIEND_BLOCKED = 4;

    private static int parseQuarter(Object quarterObj) {
        if (quarterObj instanceof String) {
            switch (((String) quarterObj).toLowerCase()) {
                case "final":
                    return Game.Q_FINAL;
                case "final overtime":
                    return Game.Q_FINAL_OT;
                case "halftime":
                    return Game.Q_HALFTIME;
                case "pregame":
                    return Game.Q_PREGAME;
                default:
                    throw new RuntimeException();
            }
        }
        return (int) quarterObj;
    }
    
    private static int parseDown(Object downObj) {
        if (downObj == JSONObject.NULL) {
            return 0;
        }
        return (int) downObj;
    }
    
    public static Login.LoginResult createLoginResult(JSONObject json) throws JSONException {
        return new Login.LoginResult(json.getString("user_id").toLowerCase(), json.getString("token"));
    }

    public static void updateFullGame(FullGame game, JSONObject json) throws JSONException {
        synchronized (game.acquireLock()) {
            json = json.getJSONObject(game.getId());
            game.setQuarter(parseQuarter(json.get("qtr")));
            game.setClock(json.getString("clock"));
            game.setDown(parseDown(json.get("down")));
            game.setToGo(json.getInt("togo"));
            game.setYardLine(json.getString("yl"));
            game.setPosTeam(json.getString("posteam"));
            game.setStadium(json.getString("stadium"));
            game.setWeather(json.getString("weather"));

            updateDriveFeed(game.getDriveFeed(), json.getJSONObject("drives"));
        }
    }

    public static void updateDriveFeed(DriveFeed driveFeed, JSONObject json) throws JSONException {
        synchronized (driveFeed.acquireLock()) {
            int driveNum = 1;
            JSONObject driveJson;
            while ((driveJson = json.optJSONObject(String.valueOf(driveNum))) != null) {
                if (driveNum <= driveFeed.numDrives()) {
                    Drive drive = driveFeed.getDrive(driveNum - 1);
                    updateDrive(drive, driveJson);
                } else {
                    Drive drive = new Drive();
                    updateDrive(drive, driveJson);
                    driveFeed.addDrive(driveNum - 1, drive);
                }
                driveNum++;
            }
        }
    }

    public static void updateDrive(Drive drive, JSONObject json) throws JSONException {
        synchronized (drive.acquireLock()) {
            drive.setQuarter(Util.parseQuarter(json.get("qtr")));
            drive.setRedZone(json.getBoolean("redzone"));
            drive.setResult(json.getString("result"));
            drive.setPosTeam(json.getString("posteam"));
            drive.setPosTime(json.getString("postime"));
            drive.setYardsGained(json.getInt("ydsgained"));

            JSONObject startJson = json.getJSONObject("start");
            drive.setStartQuarter(Util.parseQuarter(startJson.get("qtr")));
            drive.setStartClock(startJson.getString("time"));
            drive.setStartTeam(startJson.getString("team"));
            drive.setStartYardLine(startJson.getString("yrdln"));

            JSONObject endJson = json.getJSONObject("end");
            drive.setEndQuarter(Util.parseQuarter(endJson.get("qtr")));
            drive.setEndClock(endJson.getString("time"));
            drive.setEndTeam(endJson.getString("team"));
            drive.setEndYardLine(endJson.getString("yrdln"));

            JSONObject playsJson = json.getJSONObject("plays");

            LinkedList<String> playIds = new LinkedList<>();
            Iterator<String> playIterator = playsJson.keys();
            while (playIterator.hasNext())
                playIds.add(playIterator.next());

            Collections.sort(playIds);

            JSONObject playJson;
            for (int i = 0; i < playIds.size(); i++) {
                String playId = playIds.get(i);
                playJson = playsJson.getJSONObject(playId);

                if (i < drive.numPlays()) {
                    updatePlay(drive.getPlay(i), playJson);
                } else {
                    Play play = new Play();
                    updatePlay(play, playJson);
                    drive.addPlay(i, play);
                }
            }
        }
    }

    public static void updatePlay(Play play, JSONObject json) throws JSONException {
        synchronized (play.acquireLock()) {
            play.setDown(json.getInt("down"));
            play.setYardLine(json.getString("yrdln"));
            play.setQuarter(Util.parseQuarter(json.get("qtr")));
            play.setClock(json.getString("time"));
            play.setYardsToGo(json.getInt("ydstogo"));
            play.setYardsNet(json.getInt("ydsnet"));
            play.setPosTeam(json.getString("posteam"));
            play.setDescription(json.getString("desc"));
            play.setNote(json.getString("note"));

            /* It shouldn't be an issue to just create the array of SequenceItems on each update,
               because a play should really only be updated once, unlike a drive, to which plays
               are continually being appended. */
            JSONObject sequenceJson = json.getJSONObject("players");

            ArrayList<Play.SequenceItem> items = new ArrayList<>();
            Iterator<String> sequenceItemIterator = sequenceJson.keys();
            JSONArray playerSequenceItems;
            while (sequenceItemIterator.hasNext()) {
                playerSequenceItems = sequenceJson.getJSONArray(sequenceItemIterator.next());
                JSONObject sequenceItemJson;
                for (int i = 0; i < playerSequenceItems.length(); i++) {
                    sequenceItemJson = playerSequenceItems.getJSONObject(i);

                    Play.SequenceItem sequenceItem = new Play.SequenceItem();
                    sequenceItem.setSequenceNum(sequenceItemJson.getInt("sequence"));
                    sequenceItem.setTeamAbbr(sequenceItemJson.getString("clubcode"));
                    sequenceItem.setYards(sequenceItemJson.getInt("yards"));
                    sequenceItem.setStatId(sequenceItemJson.getInt("statId"));
                    sequenceItem.setPlayerName(sequenceItemJson.getString("playerName"));

                    items.add(sequenceItem);
                }
            }
            Collections.sort(items, new Comparator<Play.SequenceItem>() {
                @Override
                public int compare(Play.SequenceItem item1, Play.SequenceItem item2) {
                    return item1.getSequenceNum() - item2.getSequenceNum();
                }
            });
            play.setSequenceItems(items.toArray(new Play.SequenceItem[items.size()]));
        }
    }

    public static void updateUserDetails(UserDetails userDetails, JSONObject json) throws JSONException {
        userDetails.setUserId(json.getString("user_id").toLowerCase());
        userDetails.setUsername(json.getString("username"));
    }

    public static void updatePrediction(Prediction p, JSONObject json) throws JSONException {
        p.setUserId(json.getString("user_id").toLowerCase());
        p.setGameId(json.getString("game_id"));
        p.setAwayScore(json.getInt("away"));
        p.setHomeScore(json.getInt("home"));
    }

    public static void updateFriends(Friends friends, JSONArray friendsJson) throws JSONException {
        for (int i = 0; i < friendsJson.length(); i++) {
            JSONObject individualFriendJson = friendsJson.getJSONObject(i);
            RelationDetails relationDetails = friends.get(individualFriendJson.getString("user_id").toLowerCase());
            if (relationDetails == null)
                relationDetails = new RelationDetails();
            updateRelationDetails(relationDetails, individualFriendJson);
            friends.addRelatedUser(relationDetails);
        }
    }

//    public static void updateUserDetails(UserDetails userDetails, JSONObject json) throws JSONException {
//        userDetails.setUserId(json.getString("user_id").toLowerCase());
//        userDetails.setUsername(json.getString("username"));
//
//        userDetails.setFriendStatus(json.getInt("status"));
//    }

    public static void updateRelationDetails(RelationDetails relationDetails, JSONObject json) throws JSONException {
        relationDetails.setUserId(json.getString("user_id").toLowerCase());
        relationDetails.setUsername(json.getString("username"));
        relationDetails.setRelationStatus(json.getInt("status"));
    }

    public static FindUsersActivity.Suggestion createSuggestion(JSONObject json) throws JSONException {
        FindUsersActivity.Suggestion suggestion = new FindUsersActivity.Suggestion();
        suggestion.userId = json.getString("user_id").toLowerCase();
        suggestion.username = json.getString("username");
        return suggestion;
    }

    public static RelationDetails createFindUsersSuggestion(JSONObject json) throws JSONException {
        RelationDetails relationDetails = new RelationDetails();
        relationDetails.setUserId(json.getString("user_id").toLowerCase());
        relationDetails.setUsername(json.getString("username"));
        return relationDetails;
    }

//    public static int updateFriendUserDetails(UserDetails userDetails, JSONObject json) throws JSONException {
//        updateUserDetails(userDetails, json);
//        return json.getInt("status");
//    }

    private static final String TAG = JsonParser.class.getSimpleName();

    public static String parseLoginSuccess(JSONObject json) throws JSONException {
        return json.getString("uid");
    }

    /*
    public static User parseLoginSuccess(JSONObject json) throws JSONException {
        json = json.getJSONObject("user");
        User me = new User(json.getString("uid"));
        synchronized (me) {
            updateUserFromJson(json, me);
        }
        return me;
    }
    */

    public static int parseLoginFailure(JSONObject json) throws JSONException {
        return 0;
        // return LOGIN_ERROR_CODES[Integer.valueOf(json.getString("errno"))];
    }

    /*
    public static String[] parseActiveGamesJson(JSONArray json) throws JSONException {
        String[] active = new String[json.length()];
        for (int i = 0; i < json.length(); i++) {
            active[i] = json.getString(i);
        }
        return active;
    }
    */

    public static void updateUserDetailsFromJson(JSONObject json, User user) throws JSONException {
        /*
          {
            "uid" => 1,
            "username" => my_name,
            "email" => my_name@email.com,
            "friends" => [2, 3, 4]
          }
         */
        Log.d(TAG, "Updating user " + user.getId() + " from JSON");
        user.setUsername(json.getString("username"));
        user.setEmail(json.getString("email"));

        JSONArray friendIds = json.getJSONArray("friends");
        HashSet friends = new HashSet<String>();
        for (int i = 0; i < friendIds.length(); i++) {
            friends.add(friendIds.get(i));
        }
        user.setFriends(friends);
    }

    public static void updateUserPredictionsFromJson(JSONObject json, User user) throws JSONException {
        /*
          {
            game_id1 => {
                          "away" => 14,
                          "home" => 21
                        },
            game_id2 => {
                          "away" => 17,
                          "home" => 0,
                        }
          }
        */
        /*
        Iterator<String> iter = json.keys();
        String gameId;
        JSONObject predictions;
        while (iter.hasNext()) {
            gameId = iter.next();
            predictions = json.getJSONObject(gameId);
            user.getPredictions().put(gameId, new Prediction(predictions.getInt("away"), predictions.getInt("home")));
        }
        */
    }

    public static void updateUserFriendsFromJson(JSONObject json, User user) {

    }

    public static HashSet<String> parseActiveGamesJson(JSONArray json) throws JSONException {
        HashSet<String> active = new HashSet<>(json.length());
        for (int i = 0; i < json.length(); i++) {
            active.add(json.getString(i));
        }
        return active;
    }

    public static void updateGameFromJson(JSONObject json, NflGame game) throws JSONException {
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

    private static void updateTeamFromJson(JSONObject json, NflTeam team) throws JSONException {
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

    /*
    public static void updateUserFromJson(JSONObject json, User user) throws JSONException {
        user.setUsername(json.getString("username"));
        user.setEmail(json.getString("email"));
        // TODO: All the other things about a user
    }
    */
}
