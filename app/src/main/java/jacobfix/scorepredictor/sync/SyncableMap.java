package jacobfix.scorepredictor.sync;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;

public abstract class SyncableMap<K, V> extends HashMap<K, V> {

    private static final String TAG = SyncableMap.class.getSimpleName();

    protected BaseTask syncTask;

    private boolean syncRunning;
    private LinkedList<AsyncCallback<ArrayList<V>>> syncListeners = new LinkedList<>();

    protected AsyncCallback<ArrayList<V>> scheduledSyncCallback = new AsyncCallback<ArrayList<V>>() {
        @Override
        public void onSuccess(ArrayList<V> result) {
            notifyAllOfSuccess(result);
        }

        @Override
        public void onFailure(Exception e) {
            notifyAllOfFailure(e);
        }
    };

    public SyncableMap() {
        syncTask = getSyncTask();
    }

    public abstract BaseTask getSyncTask();

    public abstract K key(V value);

    public int getInitialDelay() {
        return 0;
    }

    public int getPeriod() {
        return 60;
    }

    public synchronized void registerSyncListener(AsyncCallback<ArrayList<V>> listener) {
        Log.d(getClass().getSimpleName(), "registering sync listener");
        if (!syncListeners.contains(listener))
            syncListeners.add(listener);

        if (!syncRunning)
            startScheduledSync();
    }

    public synchronized void unregisterSyncListener(AsyncCallback<ArrayList<V>> listener) {
        if (!syncListeners.remove(listener))
            return;

        if (syncListeners.isEmpty())
            stopScheduledSync();
    }

    private void startScheduledSync() {
        Log.d(getClass().getSimpleName(), "Starting scheduled sync!");
        if (!syncRunning) {
            syncRunning = true;
            syncTask.schedule(getInitialDelay(), getPeriod());
        } else {
            Log.wtf(TAG, "Attempted to start sync while it was already running");
        }
    }

    private void stopScheduledSync() {
        Log.d(getClass().getSimpleName(), "Stopping scheduled sync");
        if (syncRunning) {
            syncTask.stop();
            syncRunning = false;
        } else {
            Log.wtf(TAG, "Attempted to stop sync when it was not running");
        }
    }

    public void notifyAllOfSuccess(ArrayList<V> result) {
        for (AsyncCallback listener : syncListeners)
            listener.onSuccess(result);
    }

    public void notifyAllOfFailure(Exception e) {
        for (AsyncCallback listener : syncListeners)
            listener.onFailure(e);
    }

    protected TaskFinishedListener getTaskFinishedListener(final AsyncCallback<ArrayList<V>> callback) {
        return new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    callback.onFailure(task.getError());
                    return;
                }

                ArrayList<V> result = (ArrayList<V>) task.getResult();
                for (V value : result)
                    put(key(value), value);
                callback.onSuccess(result);
            }
        };
    }
}
