package jacobfix.scoreprog.server;

import org.json.JSONObject;

import java.util.Collection;


public class TestPredictionRemoteInterface extends PredictionRemoteInterface {

    @Override
    public void putPrediction(String userId, String gameId, int awayScore, int homeScore) {

    }

    @Override
    public JSONObject getPredictionsJson(Collection<String> gameIds, Collection<String> userIds) {
        return null;
    }
}
