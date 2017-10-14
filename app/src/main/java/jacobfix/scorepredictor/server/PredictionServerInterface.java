package jacobfix.scorepredictor.server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jacobfix.scorepredictor.util.NetUtil;

public abstract class PredictionServerInterface {

    public abstract JSONObject putPrediction(String uid, String gid, int awayScore, int homeScore) throws IOException, JSONException;
    public abstract JSONObject getPredictionsJson(String gid, Collection<String> uids) throws IOException, JSONException;
    public abstract JSONObject getPredictionsJson(Collection<String> gids, String uid) throws IOException, JSONException;
    public abstract JSONObject getPredictionsJson(String one, Collection<String> many, boolean oneUser) throws IOException, JSONException;

    /* Implementations. */
    public static PredictionServerInterface traditional = new PredictionServerInterface() {

        private final String GET_PREDICTIONS_URL = "http://thefixhome.com/sp/users/get_predictions.php";
        private final String PUT_PREDICTIONS_URL = "http://thefixhome.com/sp/users/put_prediction.php";

        @Override
        public JSONObject putPrediction(String uid, String gid, int awayScore, int homeScore) throws IOException, JSONException {
            Map<String, String> params = new HashMap<>();
            params.put("uid", uid);
            params.put("gid", gid);
            params.put("away", String.valueOf(awayScore));
            params.put("home", String.valueOf(homeScore));

            return new JSONObject(NetUtil.makePostRequest(PUT_PREDICTIONS_URL, params));
        }

        @Override
        public JSONObject getPredictionsJson(String gid, Collection<String> uids) throws IOException, JSONException {
            Map<String, String[]> params = new HashMap<>();
            params.put("gid", new String[]{gid});
            params.put("uid", uids.toArray(new String[uids.size()]));
            params.put("index", new String[]{"gid"});

            return new JSONObject(NetUtil.makeGetRequest(GET_PREDICTIONS_URL, params));
        }

        @Override
        public JSONObject getPredictionsJson(Collection<String> gids, String uid) throws IOException, JSONException {
            Map<String, String[]> params = new HashMap<>();
            params.put("gid", gids.toArray(new String[gids.size()]));
            params.put("uid", new String[]{uid});
            params.put("index", new String[]{"uid"});

            return new JSONObject(NetUtil.makeGetRequest(GET_PREDICTIONS_URL, params));
        }

        @Override
        public JSONObject getPredictionsJson(String one, Collection<String> many, boolean oneUser) throws IOException, JSONException {
            return (oneUser) ? getPredictionsJson(many, one) : getPredictionsJson(one, many);
        }
    };

    public static PredictionServerInterface test = new PredictionServerInterface() {

        Random random = new Random();

        @Override
        public JSONObject putPrediction(String uid, String gid, int awayScore, int homeScore) {
            return null;
        }

        @Override
        public JSONObject getPredictionsJson(Collection<String> gids, String uid) {
            return null;
        }

        @Override
        public JSONObject getPredictionsJson(String gid, Collection<String> uids) {
            return null;
        }

        @Override
        public JSONObject getPredictionsJson(String one, Collection<String> many, boolean oneUser) {
            return null;
        }

        /*
        @Override
        public JSONObject getPredictionsJson(Collection<String> gids, Collection<String> uids) throws IOException, JSONException {
            JSONObject result = new JSONObject();
            for (String uid : uids) {
                JSONObject gameJson = new JSONObject();
                for (String gid : gids) {
                    gameJson.put(gid, new JSONObject(String.format("{\"away\": %d, \"home\": %d}", random.nextInt(55), random.nextInt(55))));
                }
                result.put(uid, gameJson);
            }
            return result;
        }
        */
    };

    public static PredictionServerInterface defaultInterface = traditional;

    public static synchronized PredictionServerInterface getDefault() {
        return defaultInterface;
    }

    public static synchronized void setDefault(PredictionServerInterface newDefault) {
        defaultInterface = newDefault;
    }
}
