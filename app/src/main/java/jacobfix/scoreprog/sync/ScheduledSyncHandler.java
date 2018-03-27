package jacobfix.scoreprog.sync;


import android.util.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.TaskFinishedListener;

public class ScheduledSyncHandler<V> {

    private static final String TAG = ScheduledSyncHandler.class.getSimpleName();

    private boolean syncRunning;

    private BaseTask syncTask;
    private Set<AsyncCallback<Collection<V>>> syncListeners = new HashSet<>();

    private int initialDelay;
    private int period;

    public ScheduledSyncHandler(BaseTask task, int initialDelay, int period) {
        this.syncTask = task;
        this.initialDelay = initialDelay;
        this.period = period;
    }

    public synchronized void registerSyncListener(AsyncCallback<Collection<V>> listener) {
        if (!syncListeners.contains(listener))
            syncListeners.add(listener);

        if (!syncRunning)
            startScheduledSync();
    }

    public synchronized void unregisterSyncListener(AsyncCallback<Collection<V>> listener) {
        if (!syncListeners.remove(listener))
            return;

        if (syncListeners.isEmpty())
            stopScheduledSync();
    }

    private void startScheduledSync() {
        Log.d(getClass().getSimpleName(), "Starting scheduled sync!");
        if (!syncRunning) {
            syncRunning = true;
            syncTask.schedule(initialDelay, period);
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

    public void notifyAllOfSuccess(Collection<V> result) {
        for (AsyncCallback listener : syncListeners)
            listener.onSuccess(result);
    }

    public void notifyAllOfFailure(Exception e) {
        for (AsyncCallback listener : syncListeners)
            listener.onFailure(e);
    }
}
