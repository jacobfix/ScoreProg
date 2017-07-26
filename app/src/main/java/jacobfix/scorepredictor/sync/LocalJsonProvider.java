package jacobfix.scorepredictor.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;

import jacobfix.scorepredictor.util.Util;

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
    public JSONObject getPredictionsJson(String gid, Collection<String> uids) throws IOException, JSONException {
        JSONObject jsonIn = new JSONObject(Util.readLocalFile(PREDICTIONS_PATH));
        JSONObject jsonOut = new JSONObject();

        for (String uid : uids) {
            JSONObject allPredictionsJson = jsonIn.optJSONObject(uid);
            if (allPredictionsJson == null)
                continue;

            JSONObject predictionJson = allPredictionsJson.optJSONObject(gid);
            if (predictionJson == null)
                continue;

            JSONObject out = new JSONObject();
            out.put(gid, predictionJson);
            jsonOut.put(uid, out);
        }

        return jsonOut;
    }
}
