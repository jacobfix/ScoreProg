package jacobfix.scorepredictor;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;

public class NflGameOracle extends Oracle implements TaskFinishedListener {

    private static final String TAG = NflGameOracle.class.getSimpleName();

    private static NflGameOracle instance;

    private HashMap<String, NflGame> mActiveGames;
    private HashMap<String, NflGame> mArchivedGames;

    private TaskFinishedListener mPostSyncProcedure;

    public static int SYNC_INITIAL_DELAY = 0;
    public static int SYNC_PERIOD = 60;

    public static synchronized NflGameOracle getInstance() {
        if (instance == null) {
            instance = new NflGameOracle();
        }
        return instance;
    }

    public NflGameOracle() {
        mActiveGames = new HashMap<String, NflGame>();
        mArchivedGames = new HashMap<String, NflGame>();
    }

    @Override
    public SyncNflGamesTask initScheduledSyncTask() {
        mPostSyncProcedure = new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {

            }
        };
        return new SyncNflGamesTask(mPostSyncProcedure);
    }

    @Override
    public long getSyncInitialDelay() {
        return SYNC_INITIAL_DELAY;
    }

    @Override
    public long getSyncPeriod() {
        return SYNC_PERIOD;
    }

    public void sync() {
        new SyncNflGamesTask(this).start();
    }

    public boolean isActiveGame(String gameId) {
        return mActiveGames.containsKey(gameId);
    }

    public NflGame getActiveGame(String gameId) {
        if (isActiveGame(gameId)) {
            return mActiveGames.get(gameId);
        }
        return null;
    }

    public NflGame getArchivedGame(String gameId) {
        if (mArchivedGames.containsKey(gameId)) {
            return mArchivedGames.get(gameId);
        }
        return null;
    }

    public void setActiveGames(HashMap<String, NflGame> games) {
        this.mActiveGames = games;
    }

    public HashMap<String, NflGame> getActiveGames() {
        return this.mActiveGames;
    }

    public void onTaskFinished(BaseTask task) {
        if (task instanceof SyncNflGamesTask) {
            HashMap<String, NflGame> updated = (HashMap<String, NflGame>) task.mResult;
            /* Compare this new collection of active games to the current collection of
               active games and archive any games that are no longer active. */
            for (String gameId : mActiveGames.keySet()) {
                if (!updated.containsKey(gameId)) {
                    mArchivedGames.put(gameId, mActiveGames.remove(gameId));
                }
            }
            mActiveGames = updated;
            notifyAllSyncListeners();
        }
    }
}
