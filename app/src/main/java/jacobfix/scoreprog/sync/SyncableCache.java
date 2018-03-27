package jacobfix.scoreprog.sync;

import android.util.Log;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.task.BaseTask;

public abstract class SyncableCache<K, V> {

    private static final String TAG = SyncableCache.class.getSimpleName();

    private LinkedHashMap<K, V> cache = new LinkedHashMap<>();

    protected BaseTask syncTask;
    private boolean syncRunning;
    private LinkedList<AsyncCallback<V[]>> syncListeners = new LinkedList<>();

    public synchronized V get(K key) {
        return cache.get(key);
    }

    public synchronized void set(K key, V value) {
        cache.put(key, value);
    }

    public synchronized void reset() {
        cache.clear();
    }

    public synchronized Collection<V> getAll() {
        return cache.values();
    }

    public synchronized void registerSyncListener(AsyncCallback<V[]> listener) {
        Log.d(getClass().getSimpleName(), "registering sync listener");
        if (!syncListeners.contains(listener))
            syncListeners.add(listener);

        if (!syncRunning)
            startScheduledSync();
    }

    public synchronized void unregisterSyncListener(AsyncCallback<?> listener) {
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
            Log.e(TAG, "Attempted to start sync while it was already running");
        }
    }

    private void stopScheduledSync() {
        Log.d(getClass().getSimpleName(), "Stopping scheduled sync");
        if (syncRunning) {
            syncTask.stop();
            syncRunning = false;
        } else {
            Log.e(TAG, "Attempted to stop sync when it was not running");
        }
    }

    protected void notifyAllOfFailure(Exception e) {
        Log.d(TAG, "Notify all of failure");
        for (AsyncCallback<V[]> callback : syncListeners)
            callback.onFailure(e);
    }

    protected void notifyAllOfSuccess(V[] result) {
        Log.d(TAG, "Notify all of success");
        for (AsyncCallback<V[]> callback : syncListeners)
            callback.onSuccess(result);
    }
}
