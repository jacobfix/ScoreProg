package jacobfix.scorepredictor.task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import jacobfix.scorepredictor.Predictions;
import jacobfix.scorepredictor.sync.JsonProvider;
import jacobfix.scorepredictor.sync.SyncableCache;

public class SyncPredictionsTask extends BaseTask<Predictions[]> {

    private String gameId;
    private LinkedList<Predictions> predictions;

    public SyncPredictionsTask(String gid, Collection<Predictions> p, TaskFinishedListener listener) {
        super(listener);
        gameId = gid;
        predictions = new LinkedList<>(p);
    }

    @Override
    public void execute() {
        LinkedList<String> uids = new LinkedList<>();
        for (Predictions p : predictions)
            uids.add(p.getId());

        Predictions[] result = new Predictions[predictions.size()];
        int i = 0;

        try {
            JSONObject json = JsonProvider.getPredictionsJson(gameId, uids);

            for (Predictions p : predictions) {
                JSONObject predictionsJson = json.optJSONObject(p.getId());
                if (predictionsJson != null) {
                    p.sync(predictionsJson);
                    result[i++] = p;
                }
            }
        } catch (IOException e) {

        } catch (JSONException e) {

        } finally {
            setResult(result);
        }
    }
}
