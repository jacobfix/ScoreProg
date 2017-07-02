package jacobfix.scorepredictor.sync;

import java.util.Collection;
import java.util.HashSet;

import jacobfix.scorepredictor.Predictions;
import jacobfix.scorepredictor.ResultListener;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SyncPredictionsTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;

public class PredictionsCache extends SyncableCache<String, Predictions> {

    private String gameToSync;
    private HashSet<Predictions> predictionsToSync = new HashSet<>();

    public PredictionsCache() {
        syncTask = new SyncPredictionsTask(gameToSync, predictionsToSync, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    notifyAllOfFailure();
                    return;
                }

                Predictions[] result = (Predictions[]) task.getResult();
                for (Predictions predictions : result)
                    set(predictions.getId(), predictions);
                notifyAllOfSuccess();
            }
        });
    }

    public void sync(String gid, Collection<Predictions> predictions, final ResultListener<Predictions[]> listener) {
        new SyncPredictionsTask(gid, predictions, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    listener.onFailure(task.getError());
                    return;
                }

                Predictions[] result = (Predictions[]) task.getResult();
                for (Predictions predictions : result)
                    set(predictions.getId(), predictions);
                listener.onSuccess(result);
            }
        }).start();
    }

    public void setGameToSync(String gid) {
        gameToSync = gid;
    }

    public void addPredictionsToSync(Predictions predictions) {
        predictionsToSync.add(predictions);
    }

    public void setPredictionsToSync(Collection<Predictions> predictions) {
        predictionsToSync.clear();
        predictionsToSync.addAll(predictions);
    }

    public void clearPredictionsToSync() {
        predictionsToSync.clear();
    }
}
