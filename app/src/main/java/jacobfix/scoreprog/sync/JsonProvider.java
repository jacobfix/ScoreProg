package jacobfix.scoreprog.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;

public abstract class JsonProvider {

    private static final JsonProvider instance = new LocalJsonProvider();

    public static JsonProvider get() {
        return instance;
    }

    public abstract JSONArray getActiveGamesJson() throws IOException, JSONException;
    public abstract JSONObject getGameJson(String gid) throws IOException, JSONException;
    public abstract JSONObject getDetailsJson(Collection<String> uids) throws IOException, JSONException;
    public abstract JSONObject getPredictionsJson(Collection<String> gids, Collection<String> uids) throws IOException, JSONException;

}
