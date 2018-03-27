package jacobfix.scoreprog.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public abstract class NflGameJsonRetriever extends JsonRetriever {

    // TODO: Put the JsonParser here?

    public abstract JSONArray getActiveGamesJson() throws JSONException, IOException;
    public abstract JSONObject getIndividualGameJson(String gameId) throws JSONException, IOException;
}
