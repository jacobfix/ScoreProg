package jacobfix.scoreprog.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;

import jacobfix.scoreprog.util.Util;

public class LocalJsonProvider extends JsonProvider {

    private static final String GAME_PATH = "data/20160922-final/%s.json";
    private static final String ACTIVE_GAMES_PATH = "data/20160922-final/active.json";
    private static final String USERS_PATH = "data/users-local.json";
    private static final String PREDICTIONS_PATH = "data/predictions-local.json";

    @Override
    public JSONObject getGameJson(String gid) throws IOException, JSONException {
        return new JSONObject(Util.readLocalFile(String.format(GAME_PATH, gid)));
    }

    @Override
    public JSONArray getActiveGamesJson() throws IOException, JSONException {
        return new JSONArray(Util.readLocalFile(ACTIVE_GAMES_PATH));
    }

    @Override
    public JSONObject getDetailsJson(Collection<String> uids) throws IOException, JSONException {
        JSONObject jsonIn = new JSONObject(Util.readLocalFile(USERS_PATH));
        JSONObject jsonOut = new JSONObject();

        for (String uid : uids) {
            JSONObject detailsJson = jsonIn.optJSONObject(uid);
            if (detailsJson != null) {
                jsonOut.put(uid, detailsJson);
            }
        }

        return jsonOut;
    }

    @Override
    public JSONObject getPredictionsJson(Collection<String> gids, Collection<String> uids) throws IOException, JSONException {
        /* {
             uid_1: {gid_1: {"away": 14, "home": 21}}
         */
        JSONObject in = new JSONObject(Util.readLocalFile(PREDICTIONS_PATH));
        JSONObject out = new JSONObject();

        for (String uid : uids) {
            JSONObject userOutJson = new JSONObject();
            JSONObject gamesJson = in.optJSONObject(uid);
            if (gamesJson == null)
                continue;

            for (String gid : gids) {
                JSONObject gameJson = gamesJson.optJSONObject(gid);
                if (gameJson == null)
                    continue;

                userOutJson.put(gid, gameJson);
            }
            out.put(uid, userOutJson);
        }
        return out;
    }
}
