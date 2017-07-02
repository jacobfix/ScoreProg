package jacobfix.scorepredictor;

import org.json.JSONObject;

import java.util.HashMap;

import jacobfix.scorepredictor.sync.Syncable;

public class Predictions implements Syncable {

    private String uid;
    private HashMap<String, Prediction> predictions = new HashMap<>();

    public Predictions(String userId) {
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
    public void sync(JSONObject json) {

    }
}
