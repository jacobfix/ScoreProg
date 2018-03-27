package jacobfix.scoreprog.server;

import org.json.JSONObject;

import java.util.Collection;

public abstract class PredictionRemoteInterface {

    private static final PredictionRemoteInterface instance = new ServerPredictionRemoteInterface();

    public static PredictionRemoteInterface get() {
        return instance;
    }

    public abstract void putPrediction(String userId, String gameId, int awayScore, int homeScore);
    public abstract JSONObject getPredictionsJson(Collection<String> gameIds, Collection<String> userIds);
}
