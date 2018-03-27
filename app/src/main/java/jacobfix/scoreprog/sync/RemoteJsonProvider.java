package jacobfix.scoreprog.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scoreprog.ApplicationContext;
import jacobfix.scoreprog.util.NetUtil;

public class RemoteJsonProvider extends JsonProvider {

    private static final String GET_USERS       = "http://" + ApplicationContext.HOST + "/get_users.php";
    private static final String GET_PREDICTIONS = "http://" + ApplicationContext.HOST + "/get_predictions.php";

    public JSONObject getGameJson(String gameId) {
        return null;
    }

    public JSONArray getActiveGamesJson() {
        return null;
    }

    public JSONObject getDetailsJson(Collection<String> uids) throws IOException, JSONException {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("uid", uids.toArray(new String[uids.size()]));
        String response = NetUtil.makeGetRequest(GET_USERS, null, params);
        return new JSONObject(response);
    }

    public JSONObject getPredictionsJson(String gid, Collection<String> uids) throws IOException, JSONException {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("game_id", new String[]{gid});
        params.put("uids", uids.toArray(new String[uids.size()]));
        String response = NetUtil.makeGetRequest(GET_PREDICTIONS, null, params);
        return new JSONObject(response);
    }

    @Override
    public JSONObject getPredictionsJson(Collection<String> gids, Collection<String> uids) throws IOException, JSONException {
        return null;
    }
}
