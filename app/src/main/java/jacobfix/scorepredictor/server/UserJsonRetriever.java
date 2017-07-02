package jacobfix.scorepredictor.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;

public abstract class UserJsonRetriever extends JsonRetriever {

    // public abstract JSONObject getUserDetailsJson(Collection<String> userIds) throws IOException, JSONException;
    public abstract JSONObject getUserInfoJson(String[] userIds) throws IOException, JSONException;
    public abstract JSONObject getUserPredictionsJson(String[] userIds, String[] gameIds) throws IOException, JSONException;
}
