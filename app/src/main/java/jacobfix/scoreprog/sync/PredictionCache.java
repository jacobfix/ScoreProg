package jacobfix.scoreprog.sync;

import java.util.Collection;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.Prediction;
import jacobfix.scoreprog.task.SyncPredictionsTask;

public class PredictionCache extends SyncableMap<String, Prediction> {

    @Override
    public String key(Prediction prediction) {
        return key(prediction.getUserId(), prediction.getGameId());
    }

    public String key(String userId, String gameId) {
        return userId.concat(",").concat(gameId);
    }

    @Override
    public SyncPredictionsTask getSyncTask() {
        // predictionsToSync = new HashSet<>();
        // return new OriginalSyncPredictionsTask(predictionsToSync, getTaskFinishedListener(scheduledSyncCallback));
        return null;
    }

    public void sync(Collection<String> gameIds, AsyncCallback<Collection<Prediction>> callback) {
        new SyncPredictionsTask(gameIds, getTaskFinishedListenerForCollection(callback)).start();
    }

//    public void sync(Collection<Prediction> predictions, AsyncCallback<Collection<Prediction>> callback) {
//        new SyncPredictionsTask(predictions, getTaskFinishedListenerForCollection(callback)).start();
//    }

    public Prediction get(String userId, String gameId) {
        return get(key(userId, gameId));
    }

//    public void setPredictionsToSync(Collection<Prediction> predictions) {
//        predictionsToSync.clear();
//        predictionsToSync.addAll(predictions);
//    }
//
//    public void addPredictionToSync(Prediction prediction) {
//        predictionsToSync.add(prediction);
//    }
//
//    public void clearPredictionsToSync() {
//        predictionsToSync.clear();
//    }
}
