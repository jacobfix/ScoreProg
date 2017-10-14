package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import jacobfix.scorepredictor.sync.Syncable;

public class OriginalPredictions implements Syncable {

    private String uid;
    private HashMap<String, Prediction> predictions = new HashMap<>();

    public OriginalPredictions(String userId) {
        uid = userId;
    }

    public String getId() {
        return uid;
    }

    public void set(String gid, Prediction prediction) {
        predictions.put(gid, prediction);
    }

    public void set(String gid, int awayScore, int homeScore) {
        predictions.put(gid, new Prediction(gid, awayScore, homeScore));
    }

    public Prediction get(String gid) {
        return predictions.get(gid);
    }

    @Override
    public void sync(JSONObject json) throws JSONException {
        synchronized (this) {
            Iterator<String> gameIds = json.keys();
            while (gameIds.hasNext()) {
                String gid = gameIds.next();
                JSONObject predictionsJson = json.getJSONObject(gid);
                set(gid, predictionsJson.getInt("away"), predictionsJson.getInt("home"));
            }
        }
    }
}
