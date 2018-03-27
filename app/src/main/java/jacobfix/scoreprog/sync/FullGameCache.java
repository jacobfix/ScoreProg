package jacobfix.scoreprog.sync;

import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.FullGame;
import jacobfix.scoreprog.task.SyncFullGamesTask;

public class FullGameCache extends SyncableMap<String, FullGame> {

    private static final String TAG = FullGameCache.class.getSimpleName();

    private HashSet<FullGame> fullGamesToSync = new HashSet<>();
    private SyncFullGamesTask syncFullGamesTask = new SyncFullGamesTask(fullGamesToSync, getTaskFinishedListenerForCollection(scheduledSyncCallback));

    @Override
    public String key(FullGame game) {
        return game.getId();
    }

    @Override
    public SyncFullGamesTask getSyncTask() {
        return syncFullGamesTask;
    }

    public void sync(FullGame game, final AsyncCallback<FullGame> callback) {
        Log.d(TAG, "FullGameCache sync");
        sync(Collections.singletonList(game), new AsyncCallback<Collection<FullGame>>() {
            @Override
            public void onSuccess(Collection<FullGame> result) {
                /* Guaranteed to return FullGame objects for all Game objects provided, regardless
                   of each game's state. */
                callback.onSuccess(result.toArray(new FullGame[1])[0]);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void sync(Collection<FullGame> games, AsyncCallback<Collection<FullGame>> callback) {
        new SyncFullGamesTask(games, getTaskFinishedListenerForCollection(callback)).start();
    }

    public void setFullGamesToSync(Collection<FullGame> toSync) {
        fullGamesToSync.clear();
        fullGamesToSync.addAll(toSync);
    }

    public void addFullGameToSync(FullGame game) {
        fullGamesToSync.add(game);
    }

    public void clearFullGamesToSync() {

    }
}
