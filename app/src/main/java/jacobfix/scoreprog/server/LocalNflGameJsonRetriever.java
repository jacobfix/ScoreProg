package jacobfix.scoreprog.server;

import org.json.JSONArray;
import org.json.JSONObject;

public class LocalNflGameJsonRetriever extends NflGameJsonRetriever {

    private static final String TAG = LocalNflGameJsonRetriever.class.getSimpleName();

    @Override
    public JSONArray getActiveGamesJson() {
        return null;
    }

    @Override
    public JSONObject getIndividualGameJson(String gameId) {
        return null;
    }
}
