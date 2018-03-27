package jacobfix.scoreprog.sync;

import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.TaskFinishedListener;

public abstract class SyncableMap<K, V> extends HashMap<K, V> {

    private static final String TAG = SyncableMap.class.getSimpleName();

    private boolean syncRunning;
    private LinkedList<AsyncCallback<Collection<V>>> syncListeners = new LinkedList<>();

    protected AsyncCallback<Collection<V>> scheduledSyncCallback = new AsyncCallback<Collection<V>>() {
        @Override
        public void onSuccess(Collection<V> result) {
            notifyAllOfSuccess(result);
        }

        @Override
        public void onFailure(Exception e) {
            notifyAllOfFailure(e);
        }
    };

    public abstract BaseTask getSyncTask();

    public abstract K key(V value);

    public int getInitialDelay() {
        return 0;
    }

    public int getPeriod() {
        return 60;
    }

    public synchronized void registerSyncListener(AsyncCallback<Collection<V>> listener) {
        Log.d(getClass().getSimpleName(), "registering sync listener");
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
            getSyncTask().schedule(getInitialDelay(), getPeriod());
        } else {
            Log.wtf(TAG, "Attempted to start sync while it was already running");
        }
    }

    private void stopScheduledSync() {
        Log.d(getClass().getSimpleName(), "Stopping scheduled sync");
        if (syncRunning) {
            getSyncTask().stop();
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

    protected TaskFinishedListener getTaskFinishedListenerForSingleton(final AsyncCallback<V> callback) {
        return new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    Log.e(TAG, task.getError().toString());
                    callback.onFailure(task.getError());
                    return;
                }

                V value = (V) task.getResult();
                put(key(value), value);
                callback.onSuccess(value);
            }
        };
    }

    protected TaskFinishedListener getTaskFinishedListenerForCollection(final AsyncCallback<Collection<V>> callback) {
        return new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    Log.e(TAG, task.getError().toString());
                    callback.onFailure(task.getError());
                    return;
                }

                Collection<V> collection = (Collection<V>) task.getResult();
                for (V value : collection)
                    put(key(value), value);
                callback.onSuccess(collection);
            }
        };
    }
}
