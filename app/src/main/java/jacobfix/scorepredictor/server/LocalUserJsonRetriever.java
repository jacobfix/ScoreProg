package jacobfix.scorepredictor.server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jacobfix.scorepredictor.util.NetUtil;

public class LocalUserJsonRetriever extends UserJsonRetriever {

    private static JSONObject mAllUsersJson;
    private static JSONObject mAllPredictionsJson;

    public LocalUserJsonRetriever() {
        try {
            mAllUsersJson = NetUtil.getLocalJsonObject("data/users-local.json");
            mAllPredictionsJson = NetUtil.getLocalJsonObject("data/predictions-local.json");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject getUserInfoJson(String[] userIds) throws JSONException {
        JSONObject users = new JSONObject();
        for (String userId : userIds) {
            users.put(userId, mAllUsersJson.getJSONObject(userId));
        }
        JSONObject toReturn = new JSONObject();
        toReturn.put("users", users);
        toReturn.put("success", true);
        return toReturn;
    }

    @Override
    public JSONObject getUserPredictionsJson(String[] userIds, String[] gameIds) throws IOException, JSONException {
        JSONObject predictions = new JSONObject();
        for (String gameId : gameIds) {
            JSONObject gamePredictions = new JSONObject();
            for (String userId : userIds) {
                JSONObject predictedGames = mAllPredictionsJson.optJSONObject(userId);
                JSONObject gamePrediction = predictedGames.optJSONObject(gameId);
                if (gamePrediction != null) {
                    gamePredictions.put(userId, gamePrediction);
                }
            }
            predictions.put(gameId, gamePredictions);
        }
        JSONObject toReturn = new JSONObject();
        toReturn.put("predictions", predictions);
        toReturn.put("success", true);
        return toReturn;
    }
}
