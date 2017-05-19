package jacobfix.scorepredictor;

import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.server.LocalNflGameJsonRetriever;
import jacobfix.scorepredictor.server.NflGameJsonRetriever;
import jacobfix.scorepredictor.server.RemoteNflGameJsonRetriever;

public class NflGameOracle extends BaseOracle {

    private static final String TAG = NflGameOracle.class.getSimpleName();

    private static NflGameOracle instance;

    private HashMap<String, NflGame> mActiveGames;
    private HashMap<String, NflGame> mArchivedGames;
    private HashSet<NflGame> mGamesToSync = new HashSet<>();

    private TaskFinishedListener mPostSyncProcedure;
    private NflGameJsonRetriever mJsonRetriever;

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
    public GetActiveGamesTask initScheduledSyncTask() {
        Log.d(TAG, "initScheduledSyncTask");
        mJsonRetriever = new RemoteNflGameJsonRetriever();
        Log.d(TAG, String.valueOf(mJsonRetriever != null));
        mPostSyncProcedure = new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                // TODO: Check for task errors here?
                Collection<String> active = (Collection<String>) task.mResult;
                Log.d(TAG, "Retrieved active games: " + active.toString());
                /* First iterate through the oracle's set of active games, evicting and archiving
                   any games not present in the new list of active games. */
                for (String gameId : mActiveGames.keySet()) {
                    if (!active.contains(gameId)) {
                        mArchivedGames.put(gameId, mActiveGames.remove(gameId));
                    }
                }

                /* Then iterate through the new list of active games and add any games not present
                   in the oracle's set of active games. */
                for (String gameId : active) {
                    if (!mActiveGames.containsKey(gameId)) {
                        // TODO: Do we have to worry about this new game being accessed before its attributes are filled?
                        mActiveGames.put(gameId, new NflGame(gameId));
                    }
                }

                /* Merge the set of active games and the set of other games to sync. */
                Collection<NflGame> toSync = new HashSet<>();
                toSync.addAll(mActiveGames.values());
                toSync.addAll(mGamesToSync);

                /* After refreshing the list of active games, update the games themselves (all the
                   active games as well as any other games specified for sync). */
                new SyncNflGamesTask(toSync, mJsonRetriever, new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        notifyAllSyncListeners();
                    }
                }).start();
            }
        };
        return new GetActiveGamesTask(mJsonRetriever, mPostSyncProcedure);
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
        Log.d(TAG, "On-demand sync");
        new GetActiveGamesTask(mJsonRetriever, mPostSyncProcedure).start();
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

    public void addGameToSync(NflGame game) {
        /* Scheduled sync already updates all active games each time, so only add a game to sync if
           it is not one of the active games. */
        if (!mActiveGames.containsKey(game.getGameId()))
            mGamesToSync.add(game);
        else
            Log.d(TAG, "Attempted to add an active game to mGamesToSync");
    }

    public void clearGamesToSync() {
        mGamesToSync.clear();
    }

    public void setActiveGames(HashMap<String, NflGame> games) {
        this.mActiveGames = games;
    }

    public HashMap<String, NflGame> getActiveGames() {
        return this.mActiveGames;
    }
}
