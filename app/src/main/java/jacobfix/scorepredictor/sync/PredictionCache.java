package jacobfix.scorepredictor.sync;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SyncPredictionsTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;

public class PredictionCache extends SyncableMap<String, Prediction> {

    private HashSet<Prediction> predictionsToSync;

    @Override
    public String key(Prediction prediction) {
        return key(prediction.getUserId(), prediction.getGameId());
    }

    public String key(String userId, String gameId) {
        return userId.concat(",").concat(gameId);
    }

    @Override
    public SyncPredictionsTask getSyncTask() {
        predictionsToSync = new HashSet<>();
        return new SyncPredictionsTask(predictionsToSync, getTaskFinishedListener(scheduledSyncCallback));
    }

    public void sync(Collection<Prediction> predictions, AsyncCallback<ArrayList<Prediction>> callback) {
        new SyncPredictionsTask(predictions, getTaskFinishedListener(callback)).start();
    }

    public Prediction get(String userId, String gameId) {
        return get(key(userId, gameId));
    }

    public void setPredictionsToSync(Collection<Prediction> predictions) {
        predictionsToSync.clear();
        predictionsToSync.addAll(predictions);
    }

    public void addPredictionToSync(Prediction prediction) {
        predictionsToSync.add(prediction);
    }

    public void clearPredictionsToSync() {
        predictionsToSync.clear();
    }
}
