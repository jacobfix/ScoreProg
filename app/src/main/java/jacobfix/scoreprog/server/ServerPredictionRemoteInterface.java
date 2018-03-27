package jacobfix.scoreprog.server;

import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerPredictionRemoteInterface extends PredictionRemoteInterface {

    @Override
    public void putPrediction(String userId, String gameId, int awayScore, int homeScore) {
        Map<String, String> params = new HashMap<>();
        params.put("uid", userId);
        params.put("gid", gameId);
        params.put("away", String.valueOf(awayScore));
        params.put("home", String.valueOf(homeScore));

        //NetUtil.makePostRequest(RemoteInterface.PUT_PREDICTION_URL, params);
    }

    @Override
    public JSONObject getPredictionsJson(Collection<String> gameIds, Collection<String> userIds) {
        return null;
    }
}
