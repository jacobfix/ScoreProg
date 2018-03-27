package jacobfix.scoreprog.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import jacobfix.scoreprog.Drive;
import jacobfix.scoreprog.DriveFeed;
import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.Play;
import jacobfix.scoreprog.sync.GameAndDriveFeed;
import jacobfix.scoreprog.util.NetUtil;

import static jacobfix.scoreprog.util.Util.parseQuarter;

public abstract class GameServerInterface {

    private static final String TAG = GameServerInterface.class.getSimpleName();

    public static GameServerInterface getDefault() {
        return traditional;
    }

    public abstract JSONObject getGameJson(String gameId) throws IOException, JSONException;
    public abstract JSONObject getFullGameJson(String gameId) throws IOException, JSONException;

    public abstract void updateGameOnly(Game game, JSONObject json) throws JSONException;
    public abstract void updateGameAndDriveFeed(GameAndDriveFeed pair, JSONObject json) throws JSONException;
    public abstract void updateGame(Game game, JSONObject json) throws JSONException;
    public abstract void updateDriveFeed(DriveFeed driveFeed, JSONObject json) throws JSONException;

    protected abstract String getFullGameUrl();

    private static TraditionalGameServerInterface traditional = new TraditionalGameServerInterface();
    private static MockGameServerInterface mock = new MockGameServerInterface();

    private static class TraditionalGameServerInterface extends GameServerInterface {

        @Override
        protected String getFullGameUrl() {
            return "http://jsoftworks.com/scoreprog/games/%s.json";
        }

        @Override
        public JSONObject getFullGameJson(String gameId)  throws IOException, JSONException {
            return new JSONObject(NetUtil.makeGetRequest(String.format(getFullGameUrl(), gameId), null, null));
        }

        @Override
        public JSONObject getGameJson(String gameId) throws IOException, JSONException {
            return new JSONObject(NetUtil.makeGetRequest(String.format(getFullGameUrl(), gameId), null, null));
        }

        @Override
        public void updateGameOnly(Game game, JSONObject json) throws JSONException {
            json = json.getJSONObject(game.getId());
            updateGame(game, json);
        }

        @Override
        public void updateGameAndDriveFeed(GameAndDriveFeed pair, JSONObject json) throws JSONException {
            json = json.getJSONObject(pair.game.getId());
            updateGame(pair.game, json);
            updateDriveFeed(pair.driveFeed, json.getJSONObject("drives"));
        }

        @Override
        public void updateGame(Game game, JSONObject json) throws JSONException {
            synchronized (game.acquireLock()) {
                JSONObject awayJson = json.getJSONObject("away");
                JSONObject homeJson = json.getJSONObject("home");
                game.setAwayTeam(awayJson.getString("abbr"));
                game.setHomeTeam(homeJson.getString("abbr"));
                game.awayScore = awayJson.getJSONObject("score").getInt("T");
                game.homeScore = homeJson.getJSONObject("score").getInt("T");
                game.quarter = parseQuarter(json.get("qtr"));
                game.clock = json.getString("clock");
                game.down = parseDown(json.get("down"));
                game.toGo = json.getInt("togo");
                game.yardLine = json.getString("yl");
                game.posTeam = json.getString("posteam");
                game.stadium = json.getString("stadium");
                game.weather = json.getString("weather");
            }
        }

        @Override
        public void updateDriveFeed(DriveFeed driveFeed, JSONObject json) throws JSONException {
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

        public void updateDrive(Drive drive, JSONObject json) throws JSONException {
            synchronized (drive.acquireLock()) {
                drive.setQuarter(parseQuarter(json.get("qtr")));
                drive.setRedZone(json.getBoolean("redzone"));
                drive.setResult(json.getString("result"));
                drive.setPosTeam(json.getString("posteam"));
                drive.setPosTime(json.getString("postime"));
                drive.setYardsGained(json.getInt("ydsgained"));

                JSONObject startJson = json.getJSONObject("start");
                drive.setStartQuarter(parseQuarter(startJson.get("qtr")));
                drive.setStartClock(startJson.getString("time"));
                drive.setStartTeam(startJson.getString("team"));
                drive.setStartYardLine(startJson.getString("yrdln"));

                JSONObject endJson = json.getJSONObject("end");
                drive.setEndQuarter(parseQuarter(endJson.get("qtr")));
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

        public void updatePlay(Play play, JSONObject json) throws JSONException {
            synchronized (play.acquireLock()) {
                play.setDown(json.getInt("down"));
                play.setYardLine(json.getString("yrdln"));
                play.setQuarter(parseQuarter(json.get("qtr")));
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

        public int parseQuarter(Object quarterObject) {
            if (quarterObject instanceof String) {
                switch (((String) quarterObject).toLowerCase()) {
                    case "final":
                        return Game.Q_FINAL;
                    case "final overtime":
                        return Game.Q_FINAL_OT;
                    case "halftime":
                        return Game.Q_HALFTIME;
                    case "pregame":
                        return Game.Q_PREGAME;
                    default:
                        return Integer.parseInt((String) quarterObject);
                }
            }
            return (int) quarterObject;
        }

        private int parseDown(Object downObj) {
            if (downObj == JSONObject.NULL) {
                return 0;
            }
            return (int) downObj;
        }
    }

    private static class MockGameServerInterface extends TraditionalGameServerInterface {

        @Override
        protected String getFullGameUrl() {
            return "http://jsoftworks.com/scoreprog/mock/games/%s.json";
        }
    }

//    private static GameServerInterface traditional = new GameServerInterface() {
//
//        final String FULL_GAME_URL = "http://jsoftworks.com/scoreprog/games/%s.json";
//
//        @Override
//        public JSONObject getFullGameJson(String gameId)  throws IOException, JSONException {
//            return new JSONObject(NetUtil.makeGetRequest(String.format(FULL_GAME_URL, gameId), null, null));
//        }
//
//        @Override
//        public JSONObject getGameJson(String gameId) throws IOException, JSONException {
//            return new JSONObject(NetUtil.makeGetRequest(String.format(FULL_GAME_URL, gameId), null, null));
//        }
//
//        @Override
//        public void updateGameOnly(Game game, JSONObject json) throws JSONException {
//            json = json.getJSONObject(game.getId());
//            updateGame(game, json);
//        }
//
//        @Override
//        public void updateGameAndDriveFeed(GameAndDriveFeed pair, JSONObject json) throws JSONException {
//            json = json.getJSONObject(pair.game.getId());
//            updateGame(pair.game, json);
//            updateDriveFeed(pair.driveFeed, json.getJSONObject("drives"));
//        }
//
//        @Override
//        public void updateGame(Game game, JSONObject json) throws JSONException {
//            synchronized (game.acquireLock()) {
//                JSONObject awayJson = json.getJSONObject("away");
//                JSONObject homeJson = json.getJSONObject("home");
//                game.setAwayTeam(awayJson.getString("abbr"));
//                game.setHomeTeam(homeJson.getString("abbr"));
//                game.awayScore = awayJson.getJSONObject("score").getInt("T");
//                game.homeScore = homeJson.getJSONObject("score").getInt("T");
//                game.quarter = parseQuarter(json.get("qtr"));
//                game.clock = json.getString("clock");
//                game.down = parseDown(json.get("down"));
//                game.toGo = json.getInt("togo");
//                game.yardLine = json.getString("yl");
//                game.posTeam = json.getString("posteam");
//                game.stadium = json.getString("stadium");
//                game.weather = json.getString("weather");
//            }
//        }
//
//        @Override
//        public void updateDriveFeed(DriveFeed driveFeed, JSONObject json) throws JSONException {
//            synchronized (driveFeed.acquireLock()) {
//                int driveNum = 1;
//                JSONObject driveJson;
//                while ((driveJson = json.optJSONObject(String.valueOf(driveNum))) != null) {
//                    if (driveNum <= driveFeed.numDrives()) {
//                        Drive drive = driveFeed.getDrive(driveNum - 1);
//                        updateDrive(drive, driveJson);
//                    } else {
//                        Drive drive = new Drive();
//                        updateDrive(drive, driveJson);
//                        driveFeed.addDrive(driveNum - 1, drive);
//                    }
//                    driveNum++;
//                }
//            }
//        }
//
//        public void updateDrive(Drive drive, JSONObject json) throws JSONException {
//            synchronized (drive.acquireLock()) {
//                drive.setQuarter(parseQuarter(json.get("qtr")));
//                drive.setRedZone(json.getBoolean("redzone"));
//                drive.setResult(json.getString("result"));
//                drive.setPosTeam(json.getString("posteam"));
//                drive.setPosTime(json.getString("postime"));
//                drive.setYardsGained(json.getInt("ydsgained"));
//
//                JSONObject startJson = json.getJSONObject("start");
//                drive.setStartQuarter(parseQuarter(startJson.get("qtr")));
//                drive.setStartClock(startJson.getString("time"));
//                drive.setStartTeam(startJson.getString("team"));
//                drive.setStartYardLine(startJson.getString("yrdln"));
//
//                JSONObject endJson = json.getJSONObject("end");
//                drive.setEndQuarter(parseQuarter(endJson.get("qtr")));
//                drive.setEndClock(endJson.getString("time"));
//                drive.setEndTeam(endJson.getString("team"));
//                drive.setEndYardLine(endJson.getString("yrdln"));
//
//                JSONObject playsJson = json.getJSONObject("plays");
//
//                LinkedList<String> playIds = new LinkedList<>();
//                Iterator<String> playIterator = playsJson.keys();
//                while (playIterator.hasNext())
//                    playIds.add(playIterator.next());
//
//                Collections.sort(playIds);
//
//                JSONObject playJson;
//                for (int i = 0; i < playIds.size(); i++) {
//                    String playId = playIds.get(i);
//                    playJson = playsJson.getJSONObject(playId);
//
//                    if (i < drive.numPlays()) {
//                        updatePlay(drive.getPlay(i), playJson);
//                    } else {
//                        Play play = new Play();
//                        updatePlay(play, playJson);
//                        drive.addPlay(i, play);
//                    }
//                }
//            }
//        }
//
//        public void updatePlay(Play play, JSONObject json) throws JSONException {
//            synchronized (play.acquireLock()) {
//                play.setDown(json.getInt("down"));
//                play.setYardLine(json.getString("yrdln"));
//                play.setQuarter(parseQuarter(json.get("qtr")));
//                play.setClock(json.getString("time"));
//                play.setYardsToGo(json.getInt("ydstogo"));
//                play.setYardsNet(json.getInt("ydsnet"));
//                play.setPosTeam(json.getString("posteam"));
//                play.setDescription(json.getString("desc"));
//                play.setNote(json.getString("note"));
//
//            /* It shouldn't be an issue to just create the array of SequenceItems on each update,
//               because a play should really only be updated once, unlike a drive, to which plays
//               are continually being appended. */
//                JSONObject sequenceJson = json.getJSONObject("players");
//
//                ArrayList<Play.SequenceItem> items = new ArrayList<>();
//                Iterator<String> sequenceItemIterator = sequenceJson.keys();
//                JSONArray playerSequenceItems;
//                while (sequenceItemIterator.hasNext()) {
//                    playerSequenceItems = sequenceJson.getJSONArray(sequenceItemIterator.next());
//                    JSONObject sequenceItemJson;
//                    for (int i = 0; i < playerSequenceItems.length(); i++) {
//                        sequenceItemJson = playerSequenceItems.getJSONObject(i);
//
//                        Play.SequenceItem sequenceItem = new Play.SequenceItem();
//                        sequenceItem.setSequenceNum(sequenceItemJson.getInt("sequence"));
//                        sequenceItem.setTeamAbbr(sequenceItemJson.getString("clubcode"));
//                        sequenceItem.setYards(sequenceItemJson.getInt("yards"));
//                        sequenceItem.setStatId(sequenceItemJson.getInt("statId"));
//                        sequenceItem.setPlayerName(sequenceItemJson.getString("playerName"));
//
//                        items.add(sequenceItem);
//                    }
//                }
//                Collections.sort(items, new Comparator<Play.SequenceItem>() {
//                    @Override
//                    public int compare(Play.SequenceItem item1, Play.SequenceItem item2) {
//                        return item1.getSequenceNum() - item2.getSequenceNum();
//                    }
//                });
//                play.setSequenceItems(items.toArray(new Play.SequenceItem[items.size()]));
//            }
//        }
//
//        public int parseQuarter(Object quarterObject) {
//            if (quarterObject instanceof String) {
//                switch (((String) quarterObject).toLowerCase()) {
//                    case "final":
//                        return Game.Q_FINAL;
//                    case "final overtime":
//                        return Game.Q_FINAL_OT;
//                    case "halftime":
//                        return Game.Q_HALFTIME;
//                    case "pregame":
//                        return Game.Q_PREGAME;
//                    default:
//                        return Integer.parseInt((String) quarterObject);
//                }
//            }
//            return (int) quarterObject;
//        }
//
//        private int parseDown(Object downObj) {
//            if (downObj == JSONObject.NULL) {
//                return 0;
//            }
//            return (int) downObj;
//        }
//    };
}
