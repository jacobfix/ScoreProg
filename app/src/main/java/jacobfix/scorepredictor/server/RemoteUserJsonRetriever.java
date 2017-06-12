package jacobfix.scorepredictor.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.util.NetUtil;

public class RemoteUserJsonRetriever extends UserJsonRetriever {

    private static final String TAG = RemoteUserJsonRetriever.class.getSimpleName();

    // private static final String GET_USERS_URL = "http://192.168.1.15/get_users.php";
    // private static final String GET_USER_FRIENDS_BASE_URL = "http://192.168.1.15/get_friends.php";
    // private static final String GET_USER_PREDICTIONS_URL = "http://192.168.1.15/get_predictions.php";

    private static final String GET_USERS_URL = "http://172.20.8.156/get_users.php";
    // private static final String GET_USER_FRIENDS_BASE_URL = "http://192.168.1.15/get_friends.php";
    private static final String GET_USER_PREDICTIONS_URL = "http://172.20.8.156/get_predictions.php";

    /* @Override
    public JSONObject getUserDetailsJson(String[] userIds) throws IOException, JSONException {
        StringBuilder query = new StringBuilder("?uid=");
        for (String userId : userIds) {
            query.append(String.format("%s_", URLEncoder.encode(userId, "UTF-8")));
        }
        query.deleteCharAt(query.length() - 1);
        Log.d(TAG, "This is our URL: " + GET_USERS_BASE_URL + query);
        return NetUtil.getJsonObjectFromUrl(GET_USERS_BASE_URL + query);
        // TODO: Maybe get an array and then iterate over that, making it an object
    }*/

    @Override
    public JSONObject getUserDetailsJson(String[] userIds) throws IOException, JSONException {
        HashMap<String, String[]> params = new HashMap<>();
        params.put("uid", userIds);
        // TODO: Change this to makeGetRequest, the result of which is cast to a JSONObject
        return NetUtil.getJsonObjectFromUrl(GET_USERS_URL + NetUtil.makeGetString(params));
    }

    @Override
    public JSONObject getUserPredictionsJson(String[] userIds, String[] gameIds) throws IOException, JSONException {
        /* JSONObject result = new JSONObject();
        HashMap<String, String[]> params = new HashMap<>();
        for (String userId : userIds) {
            params.put("uid", new String[]{userId});
            params.put("games", gameIds);
            String response = NetUtil.makeGetRequest(GET_USER_PREDICTIONS_URL, params);
            result.put(userId, new JSONObject(response));
        }
        return result; */
        JSONObject result = new JSONObject();
        HashMap<String, String[]> params = new HashMap<>();
        for (String gameId : gameIds) {
            params.put("game_id", new String[]{gameId});
            params.put("uids", userIds);
            String response = NetUtil.makeGetRequest(GET_USER_PREDICTIONS_URL, params);
            result.put(gameId, new JSONObject(response));
        }
        return result;
    }

    /*
    @Override
    public JSONObject getUserFriendsJson(Collection<String> userIds) throws IOException, JSONException {
        StringBuilder query = new StringBuilder("?uid=");
        for (String userId : userIds) {
            query.append(String.format("%s_", URLEncoder.encode(userId, "UTF-8")));
        }
        query.deleteCharAt(query.length() - 1);
        return NetUtil.getJsonObjectFromUrl(GET_USER_FRIENDS_BASE_URL + query);
    }
    */
}
