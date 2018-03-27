package jacobfix.scoreprog.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jacobfix.scoreprog.LocalAccountManager;
import jacobfix.scoreprog.Participants;
import jacobfix.scoreprog.Prediction;
import jacobfix.scoreprog.components.ErrorMessageInputField;
import jacobfix.scoreprog.util.NetUtil;

public abstract class PredictionServerInterface {

    private static final String TAG = PredictionServerInterface.class.getSimpleName();

    public abstract JSONObject submitPrediction(String gameId, int awayScore, int homeScore) throws IOException, JSONException;
    public abstract JSONArray getMyPredictions(Collection<String> gameIds) throws Exception;
    public abstract JSONArray getFriendPredictions(String gameId) throws Exception;
    public abstract JSONArray getFriendPredictions(Collection<String> gameIds) throws Exception;

    public abstract void updatePrediction(Prediction prediction, JSONObject json) throws JSONException;
    public abstract void updateParticipants(Participants participants, JSONArray json) throws JSONException;

    public static PredictionServerInterface traditional = new PredictionServerInterface() {

        final String BASE_URL = "http://jsoftworks.com/scoreprog/";
        final String SUBMIT_PREDICTION_URL = BASE_URL.concat("submit_prediction.php");
        final String GET_PREDICTIONS_URL = BASE_URL.concat("get_predictions.php");

        @Override
        public JSONObject submitPrediction(String gameId, int awayScore, int homeScore) throws IOException, JSONException {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            params.put("game_id", new String[]{gameId});
            params.put("away", new String[]{String.valueOf(awayScore)});
            params.put("home", new String[]{String.valueOf(homeScore)});

            return new JSONObject(NetUtil.makePostRequest(SUBMIT_PREDICTION_URL, headers, params));
        }

        @Override
        public JSONArray getMyPredictions(Collection<String> gameIds) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            params.put("game_ids", gameIds.toArray(new String[gameIds.size()]));
            params.put("src", new String[]{"me"});

            JSONObject response = new JSONObject(NetUtil.makeGetRequest(GET_PREDICTIONS_URL, headers, params));
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return response.getJSONArray("payload");
        }

        @Override
        public JSONArray getFriendPredictions(String gameId) throws Exception {
            return getFriendPredictions(Collections.singletonList(gameId));
        }

        @Override
        public JSONArray getFriendPredictions(Collection<String> gameIds) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            params.put("game_ids", gameIds.toArray(new String[gameIds.size()]));
            params.put("src", new String[]{"friends"});

            JSONObject response = new JSONObject(NetUtil.makeGetRequest(GET_PREDICTIONS_URL, headers, params));
            Log.d(TAG, "Just retrieved response: " + response.toString());
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return response.getJSONArray("payload");
        }

        @Override
        public void updatePrediction(Prediction prediction, JSONObject json) throws JSONException {
            synchronized (prediction.acquireLock()) {
                prediction.setUserId(json.getString("user_id").toLowerCase());
                prediction.setGameId(json.getString("game_id"));
                prediction.setAwayScore(json.getInt("away"));
                prediction.setHomeScore(json.getInt("home"));
            }
        }

        @Override
        public void updateParticipants(Participants participants, JSONArray json) throws JSONException {
            Set<Prediction> predictions = new HashSet<>();
            for (int i = 0; i < json.length(); i++) {
                JSONObject predictionJson = json.getJSONObject(i);
                /* You might as well just make new ones each time. */
                Prediction prediction = new Prediction();
                updatePrediction(prediction, predictionJson);
                predictions.add(prediction);
            }

            synchronized (participants.acquireLock()) {
                participants.addAll(predictions);
            }
        }
    };

    public static PredictionServerInterface defaultInterface = traditional;

    public static synchronized PredictionServerInterface getDefault() {
        return defaultInterface;
    }

    public static synchronized void setDefault(PredictionServerInterface newDefault) {
        defaultInterface = newDefault;
    }

}
