package jacobfix.scorepredictor.task;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.Predictions;
import jacobfix.scorepredictor.server.PredictionServerInterface;

public class SyncPredictionsTask extends BaseTask<ArrayList<Prediction>> {

    private static final String TAG = SyncPredictionsTask.class.getSimpleName();

    private HashSet<Prediction> predictions;

    public SyncPredictionsTask(Collection<Prediction> predictions, TaskFinishedListener listener) {
        super(listener);
        this.predictions = new HashSet<>(predictions);
    }

    @Override
    public void execute() {
        ArrayList<Prediction> result;
        try {
            HashSet<String> userIds = new HashSet<>();
            HashSet<String> gameIds = new HashSet<>();
            for (Prediction p : predictions) {
                synchronized (p.getLock()) {
                    if (p.isModified()) {
                        if (p.isOnServer()) {
                            p.setModified(false);
                            userIds.add(p.getUserId());
                            gameIds.add(p.getGameId());
                        } else {
                            predictions.remove(p);
                        }
                    } else {
                        userIds.add(p.getUserId());
                        gameIds.add(p.getGameId());
                    }
                }
            }

            if (userIds.size() < gameIds.size())
                result = go(true, userIds, gameIds);
            else
                result = go(false, gameIds, userIds);

            Log.d(TAG, "Synced result: " + result.toString());
            setResult(result);
        } catch (Exception e) {
            reportError(e);
            e.printStackTrace();
        }
    }

    private ArrayList<Prediction> go(boolean indexByUser, Collection<String> iterateOver, Collection<String> other) throws IOException, JSONException {
        ArrayList<Prediction> synced = new ArrayList<>();
        Predictions set = new Predictions(indexByUser);
        for (Prediction p : predictions)
            set.put(p);

        for (String item : iterateOver) {
            JSONObject json = PredictionServerInterface.getDefault().getPredictionsJson(item, other, indexByUser);
            Log.d(TAG, "JSON: " + json.toString());
            if (!json.getBoolean("success"))
                throw new IOException(String.valueOf(json.getInt("errno")));

            HashMap<String, JSONObject> indexedJson = new HashMap<>();
            JSONArray allPredictionsJson = json.getJSONArray("predictions");
            for (int i = 0; i < allPredictionsJson.length(); i++) {
                JSONObject singlePredictionJson = allPredictionsJson.getJSONObject(i);
                String key = (indexByUser) ? "gameid" : "userid";
                indexedJson.put(singlePredictionJson.getString(key), singlePredictionJson);
            }
            Log.d(TAG, indexedJson.entrySet().toString());

            LinkedList<Prediction> existing = set.get(item);
            Log.d(TAG, "Existing: " + existing.toString());
            for (Prediction p : existing) {
                String id = (indexByUser) ? p.getGameId() : p.getUserId();
                JSONObject predictionJson = indexedJson.get(id);
                if (predictionJson != null && !p.updatesDisabled()) {
                    synchronized (p.getLock()) {
                        p.updateFromJson(predictionJson);
                    }
                }
                synced.add(p);
            }
        }
        Log.d(TAG, "Synced: " + synced.toString());
        return synced;
    }
}
