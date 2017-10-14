package jacobfix.scorepredictor.server;

import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.util.NetUtil;

public abstract class PredictionRemoteInterface {

    private static final PredictionRemoteInterface instance = new ServerPredictionRemoteInterface();

    public static PredictionRemoteInterface get() {
        return instance;
    }

    public abstract void putPrediction(String userId, String gameId, int awayScore, int homeScore);
    public abstract JSONObject getPredictionsJson(Collection<String> gameIds, Collection<String> userIds);
}
