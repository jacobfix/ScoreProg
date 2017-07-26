package jacobfix.scorepredictor.sync;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;

public class SyncableMap<K, V> extends HashMap {

    private static final String TAG = SyncableMap.class.getSimpleName();

    protected BaseTask syncTask;

    private boolean syncRunning;
    private LinkedList<AsyncCallback<V[]>> syncListeners = new LinkedList<>();

    public synchronized void registerSyncListener(AsyncCallback<V[]> listener) {
        Log.d(getClass().getSimpleName(), "registering sync listener");
        if (!syncListeners.contains(listener))
            syncListeners.add(listener);

        if (!syncRunning)
            startScheduledSync();
    }

    public synchronized void unregisterSyncListener(AsyncCallback<V[]> listener) {
        if (!syncListeners.remove(listener))
            return;

        if (syncListeners.isEmpty())
            stopScheduledSync();
    }

    private void startScheduledSync() {
        Log.d(getClass().getSimpleName(), "Starting scheduled sync!");
        if (!syncRunning) {
            syncRunning = true;
            syncTask.schedule(0, 60);
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

    public void notifyAllOfFailure(Exception e) {
        for (AsyncCallback listener : syncListeners)
            listener.onFailure(e);
    }

    public void notifyAllOfSuccess(V[] result) {
        for (AsyncCallback listener : syncListeners)
            listener.onSuccess(result);
    }

}
