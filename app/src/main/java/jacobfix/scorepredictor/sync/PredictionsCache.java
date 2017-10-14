package jacobfix.scorepredictor.sync;

import java.util.Collection;
import java.util.HashSet;

import jacobfix.scorepredictor.OriginalPredictions;
import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.OriginalSyncPredictionsTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;

public class PredictionsCache extends SyncableCache<String, OriginalPredictions> {

    private OriginalSyncPredictionsTask scheduledTask;
    private AsyncCallback<OriginalPredictions[]> scheduledCallback;

    private HashSet<String> gamesToSync = new HashSet<String>();
    private HashSet<OriginalPredictions> predictionsToSync = new HashSet<>();

    public PredictionsCache() {
        scheduledCallback = new AsyncCallback<OriginalPredictions[]>() {
            @Override
            public void onSuccess(OriginalPredictions[] result) {
                notifyAllOfSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                notifyAllOfFailure(e);
            }
        };
        scheduledTask = new OriginalSyncPredictionsTask(gamesToSync, predictionsToSync, getTaskFinishedListener(scheduledCallback));
    }

    public void sync(Collection<String> gids, Collection<OriginalPredictions> predictions, final AsyncCallback<OriginalPredictions[]> callback) {
        new OriginalSyncPredictionsTask(gids, predictions, getTaskFinishedListener(callback)).start();
    }

    public void addGameToSync(String gid) {
        gamesToSync.add(gid);
    }

    public void setGamesToSync(Collection<String> gids) {
        gamesToSync.clear();
        gamesToSync.addAll(gids);
    }

    public void clearGamesToSync() {
        gamesToSync.clear();
    }

    public void addPredictionsToSync(OriginalPredictions predictions) {
        predictionsToSync.add(predictions);
    }

    public void setPredictionsToSync(Collection<OriginalPredictions> predictions) {
        predictionsToSync.clear();
        predictionsToSync.addAll(predictions);
    }

    public void clearPredictionsToSync() {
        predictionsToSync.clear();
    }

    private TaskFinishedListener getTaskFinishedListener(final AsyncCallback<OriginalPredictions[]> callback) {
        return new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    callback.onFailure(task.getError());
                    return;
                }

                OriginalPredictions[] result = (OriginalPredictions[]) task.getResult();
                for (OriginalPredictions predictions : result)
                    set(predictions.getId(), predictions);
                callback.onSuccess(result);
            }
        };
    }
}
