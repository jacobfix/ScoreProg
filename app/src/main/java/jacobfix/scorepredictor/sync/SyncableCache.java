package jacobfix.scorepredictor.sync;

import android.util.Log;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import jacobfix.scorepredictor.ResultListener;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SyncDetailsTask;
import jacobfix.scorepredictor.task.SyncTask;

public abstract class SyncableCache<K, V extends Syncable> {

    private static final String TAG = SyncableCache.class.getSimpleName();

    private LinkedHashMap<K, V> cache = new LinkedHashMap<>();

    protected BaseTask syncTask;
    private boolean syncRunning;
    private LinkedList<ResultListener<?>> syncListeners = new LinkedList<>();

    public synchronized V get(K key) {
        return cache.get(key);
    }

    public synchronized void set(K key, V value) {
        cache.put(key, value);
    }

    public synchronized void reset() {
        cache.clear();
    }

    public synchronized void registerSyncListener(ResultListener<?> listener) {
        if (!syncListeners.contains(listener))
            syncListeners.add(listener);

        if (!syncRunning)
            startScheduledSync();
    }

    public synchronized void unregisterSyncListener(ResultListener<?> listener) {
        if (!syncListeners.remove(listener))
            return;

        if (syncListeners.isEmpty())
            stopScheduledSync();
    }

    private void startScheduledSync() {
        if (!syncRunning) {
            syncRunning = true;
            syncTask.schedule(0, 60);
        } else {
            Log.e(TAG, "Attempted to start sync while it was already running");
        }
    }

    private void stopScheduledSync() {
        if (syncRunning) {
            syncTask.stop();
            syncRunning = false;
        } else {
            Log.e(TAG, "Attempted to stop sync when it was not running");
        }
    }

    protected void notifyAllOfFailure() {

    }

    protected void notifyAllOfSuccess() {

    }
}
