package jacobfix.scorepredictor.deprecated;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import jacobfix.scorepredictor.sync.SyncListener;

/** 
 * Handles fixed refreshes as well as user-requested refreshes.
 */
public class NflGameSyncManager {

    private static final String TAG = NflGameSyncManager.class.getSimpleName();

    private static NflGameSyncManager mInstance;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService mScheduledExecutor = Executors.newScheduledThreadPool(1);

    private Future<?> mSyncHandle;
    private ScheduledFuture<?> mScheduledSyncHandle;

    private OriginalNflGameSyncTask mSyncTask;
    private SyncListener mSyncFinishedListener;
    private BroadcastReceiver mSyncFinishedReceiver;
    private boolean mRunning;

    private DataRetriever mDataRetriever;

    public static final int SOURCE_NFL_WEBSITE = 1;
    public static final int SOURCE_LOCAL_PREGAME = 2;
    public static final int SOURCE_LOCAL_IN_PROGRESS = 3;
    public static final int SOURCE_LOCAL_FINAL = 4;

    public static final int DEFAULT_SOURCE = SOURCE_NFL_WEBSITE;

    // boolean indicating first run? so if the game is final we only download the detailed JSON once?
    private Context mContext;

    private static final int INITIAL_DELAY = 0;
    private static final int PERIOD = 60;

    public static final String ACTION_ANNOUNCE_SYNC_FINISHED = "sync_fin";

    public static synchronized NflGameSyncManager get(Context context) {
        if (mInstance == null) {
            mInstance = new NflGameSyncManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public NflGameSyncManager(Context context) {
        // mSyncFinishedListener = listener;
        mContext = context;
        // mDataRetriever = new LocalDataRetriever(context, LocalDataRetriever.SOURCE_20160922_PREGAME);
        mDataRetriever = new NflWebsiteDataRetriever(context);
        mSyncTask = new OriginalNflGameSyncTask(context, mDataRetriever);
        mSyncFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Finished sync for " + mSyncFinishedListener.getClass().getSimpleName());
                mSyncFinishedListener.onSyncFinished();
            }
        };
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(mSyncFinishedReceiver, new IntentFilter(ACTION_ANNOUNCE_SYNC_FINISHED));
    }

    public void sync() {
        mExecutor.submit(mSyncTask);
        // mScheduledExecutor.submit(mSyncTask); // Just do this instead?
    }

    public void setSource(int source) {
        switch (source) {
            case SOURCE_NFL_WEBSITE:
                mDataRetriever = new NflWebsiteDataRetriever(mContext);
                break;
            case SOURCE_LOCAL_PREGAME:
                mDataRetriever = new LocalDataRetriever(mContext, LocalDataRetriever.SOURCE_20160922_PREGAME);
                break;
            case SOURCE_LOCAL_IN_PROGRESS:
                mDataRetriever = new LocalDataRetriever(mContext, LocalDataRetriever.SOURCE_20160922_IN_PROGRESS);
                break;
            case SOURCE_LOCAL_FINAL:
                mDataRetriever = new LocalDataRetriever(mContext, LocalDataRetriever.SOURCE_20160922_FINAL);
                break;
        }
        mSyncTask.setDataRetriever(mDataRetriever);
        mSyncTask.setFirstSync(true);
        Log.d(TAG, "Changed data retriever");
    }

    public void startScheduledSync(SyncListener listener) {
        mSyncFinishedListener = listener;
        if (!mRunning) {
            mSyncHandle = mScheduledExecutor.scheduleAtFixedRate(mSyncTask, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);
            mRunning = true;
            Log.d(TAG, "Sync started in " + mSyncFinishedListener.getClass());
        } else {
            Log.d(TAG, "Attempted to start sync while it was already running");
        }
    }

    public void stopScheduledSync(SyncListener listener) {
        if (listener == mSyncFinishedListener) {
            if (mRunning) {
                mSyncHandle.cancel(true);
                // mScheduledExecutor.shutdown();
                mRunning = false;
                Log.d(TAG, "Sync stopped in " + mSyncFinishedListener.getClass());
            } else {
                Log.d(TAG, "Attempted to stop sync when it was not running");
            }
        }
    }

    public void setListener(SyncListener listener) {
        mSyncFinishedListener = listener;
    }

    public boolean isRunning() {
        return mRunning;
    }


}
