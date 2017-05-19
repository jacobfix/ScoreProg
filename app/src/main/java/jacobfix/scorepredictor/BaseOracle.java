package jacobfix.scorepredictor;

import android.util.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.sync.SyncFinishedListener;

public abstract class BaseOracle {

    private static final String TAG = BaseOracle.class.getSimpleName();

    protected BaseTask mScheduledSyncTask;
    protected Collection<SyncFinishedListener> mSyncListeners;
    protected boolean mSyncRunning;

    protected long mSyncInitialDelay;
    protected long mSyncPeriod;

    public BaseOracle() {
        mSyncListeners = new HashSet<>();
        mScheduledSyncTask = initScheduledSyncTask();
        mSyncInitialDelay = getSyncInitialDelay();
        mSyncPeriod = getSyncPeriod();
    }

    public void registerSyncListener(SyncFinishedListener listener) {
        if (!mSyncListeners.contains(listener)) {
            mSyncListeners.add(listener);
        }
        /* Scheduled sync is guaranteed to be running after this method is called. */
        if (!mSyncRunning) {
            startScheduledSync(mSyncInitialDelay, mSyncPeriod);
        }
    }

    public void unregisterSyncListener(SyncFinishedListener listener) {
        if (!mSyncListeners.remove(listener)) {
            return;
        }
        if (mSyncListeners.isEmpty()) {
            stopScheduledSync();
        }
    }

    public void startScheduledSync(long delay, long period) {
        Log.d(TAG, "Starting scheduled sync");
        if (!mSyncRunning) {
            mSyncRunning = true;
            mScheduledSyncTask.schedule(delay, period);
        } else {
            Log.d(TAG, "Attempted to start sync while it was already running");
        }
    }

    public void stopScheduledSync() {
        Log.d(TAG, "Stopping scheduled sync");
        if (mSyncRunning) {
            mScheduledSyncTask.stop();
            mSyncRunning = false;
        } else {
            Log.d(TAG, "Attempted to stop sync when it was not running");
        }
    }

    protected void notifyAllSyncListeners() {
        for (SyncFinishedListener listener : mSyncListeners) {
            listener.onSyncFinished();
        }
    }

    public abstract BaseTask initScheduledSyncTask();
    public abstract long getSyncInitialDelay();
    public abstract long getSyncPeriod();
}
