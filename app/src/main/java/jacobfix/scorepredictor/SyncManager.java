package jacobfix.scorepredictor;

import android.util.Log;

import java.util.Collection;
import java.util.HashSet;

import jacobfix.scorepredictor.sync.SyncListener;
import jacobfix.scorepredictor.task.BaseTask;

public abstract class SyncManager {

    private static String TAG = SyncManager.class.getSimpleName();

    protected BaseTask mScheduledTask;
    protected Collection<SyncListener> mSyncListeners;
    protected boolean mSyncRunning;

    protected long mSyncInitialDelay;
    protected long mSyncPeriod;

    public SyncManager() {
        mSyncListeners = new HashSet<>();
    }

    public void registerSyncListener(SyncListener listener) {
        if (!mSyncListeners.contains(listener)) {
            mSyncListeners.add(listener);
        }
        /* Scheduled sync is guaranteed to be running after this method is called. */
        if (!mSyncRunning) {
            startScheduledSync(mSyncInitialDelay, mSyncPeriod);
        }
    }

    public void unregisterSyncListener(SyncListener listener) {
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
            mScheduledTask.schedule(delay, period);
        } else {
            Log.d(TAG, "Attempted to start sync while it was already running");
        }
    }

    public void stopScheduledSync() {
        Log.d(TAG, "Stopping scheduled sync");
        if (mSyncRunning) {
            mScheduledTask.stop();
            mSyncRunning = false;
        } else {
            Log.d(TAG, "Attempted to stop sync when it was not running");
        }
    }

    public void notifyAllOfFinish() {
        for (SyncListener listener : mSyncListeners)
            listener.onSyncFinished();
    }

    public void notifyAllOfError() {
        for (SyncListener listener : mSyncListeners)
            listener.onSyncError();
    }
}
