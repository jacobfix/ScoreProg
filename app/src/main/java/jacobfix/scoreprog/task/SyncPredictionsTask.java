package jacobfix.scoreprog.task;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import jacobfix.scoreprog.Prediction;
import jacobfix.scoreprog.server.JsonParser;
import jacobfix.scoreprog.server.PredictionServerInterface;

public class SyncPredictionsTask extends BaseTask<Collection<Prediction>> {

    private static final String TAG = SyncPredictionsTask.class.getSimpleName();

    public ArrayList<String> gameIds;

    public SyncPredictionsTask(Collection<String> gameIds, TaskFinishedListener listener) {
        super(listener);
        this.gameIds = new ArrayList<>(gameIds);
    }

    @Override
    public void execute() {
        try {
            HashSet<Prediction> predictions = new HashSet<>();

            JSONArray json = PredictionServerInterface.getDefault().getMyPredictions(this.gameIds);
            for (int i = 0; i < json.length(); i++) {
                Prediction prediction = new Prediction();
                JsonParser.updatePrediction(prediction, json.getJSONObject(i));
                predictions.add(prediction);
            }

            setResult(predictions);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
