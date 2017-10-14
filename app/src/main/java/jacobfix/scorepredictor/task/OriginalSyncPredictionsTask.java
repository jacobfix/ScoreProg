package jacobfix.scorepredictor.task;

import android.util.Log;

import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

import jacobfix.scorepredictor.OriginalPredictions;
import jacobfix.scorepredictor.server.PredictionServerInterface;

public class OriginalSyncPredictionsTask extends BaseTask<OriginalPredictions[]> {

    private static final String TAG = OriginalSyncPredictionsTask.class.getSimpleName();

    private LinkedList<String> gids = new LinkedList<>();
    private LinkedList<OriginalPredictions> predictions = new LinkedList<>();

    private LinkedList<String> uids = new LinkedList<>();

    public OriginalSyncPredictionsTask(Collection<String> gids, Collection<OriginalPredictions> predictions, TaskFinishedListener listener) {
        super(listener);
        this.gids.addAll(gids);
        this.predictions.addAll(predictions);
    }

    @Override
    public void execute() {
        for (OriginalPredictions p : predictions)
            uids.add(p.getId());

        LinkedList<OriginalPredictions> result = new LinkedList<>();

        try {
            Thread.sleep(3000);
            // JSONObject predictionsJson = PredictionServerInterface.getDefault().getPredictionsJson(gids, uids);
            JSONObject predictionsJson = null;
            if (predictionsJson == null)
                Log.wtf(TAG, "predictions JSON was null!");

            for (OriginalPredictions p : predictions) {
                JSONObject userPredictionsJson = predictionsJson.optJSONObject(p.getId());
                if (userPredictionsJson != null) {
                    p.sync(userPredictionsJson);
                    result.add(p);
                }
            }
        } catch (Exception e) {
            reportError(e);
        } finally {
            setResult(result.toArray(new OriginalPredictions[result.size()]));
        }
    }
}
