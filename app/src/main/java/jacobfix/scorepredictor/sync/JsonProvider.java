package jacobfix.scorepredictor.sync;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.ApplicationContext;
import jacobfix.scorepredictor.util.NetUtil;

public class JsonProvider {

    private static final String GET_USERS       = "http://" + ApplicationContext.HOST + "/get_users.php";
    private static final String GET_PREDICTIONS = "http://" + ApplicationContext.HOST + "/get_predictions.php";

    public static JSONObject getDetailsJson(Collection<String> uids) throws IOException, JSONException {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("uid", uids.toArray(new String[uids.size()]));
        String response = NetUtil.makeGetRequest(GET_USERS, params);
        return new JSONObject(response);
    }

    public static JSONObject getPredictionsJson(String gid, Collection<String> uids) throws IOException, JSONException {
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("game_id", new String[]{gid});
        params.put("uids", uids.toArray(new String[uids.size()]));
        String response = NetUtil.makeGetRequest(GET_PREDICTIONS, params);
        return new JSONObject(response);
    }
}
