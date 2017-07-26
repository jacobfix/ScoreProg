package jacobfix.scorepredictor.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.ApplicationContext;
import jacobfix.scorepredictor.util.NetUtil;

public abstract class JsonProvider {

    private static final JsonProvider instance = new LocalJsonProvider();

    public static JsonProvider get() {
        return instance;
    }

    public abstract JSONArray getActiveGamesJson() throws IOException, JSONException;
    public abstract JSONObject getGameJson(String gid) throws IOException, JSONException;
    public abstract JSONObject getDetailsJson(Collection<String> uids) throws IOException, JSONException;
    public abstract JSONObject getPredictionsJson(String gid, Collection<String> uids) throws IOException, JSONException;

}
