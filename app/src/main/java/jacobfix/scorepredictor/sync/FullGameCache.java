package jacobfix.scorepredictor.sync;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.FullGame;
import jacobfix.scorepredictor.task.SyncFullGamesTask;

public class FullGameCache extends SyncableMap<String, FullGame> {

    private static final String TAG = FullGameCache.class.getSimpleName();

    private HashSet<FullGame> gamesToSync;

    @Override
    public String key(FullGame game) {
        return game.getId();
    }

    @Override
    public SyncFullGamesTask getSyncTask() {
        gamesToSync = new HashSet<>();
        return new SyncFullGamesTask(gamesToSync, getTaskFinishedListener(scheduledSyncCallback));
    }

    public void sync(FullGame game, final AsyncCallback<FullGame> callback) {
        Log.d(TAG, "Syncing: " + game.getId());
        sync(Collections.singletonList(game), new AsyncCallback<ArrayList<FullGame>>() {
            @Override
            public void onSuccess(ArrayList<FullGame> result) {
                /* Guaranteed to return FullGame objects for all Game objects provided, regardless
                   of each game's state. */
                callback.onSuccess(result.get(0));
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void sync(Collection<FullGame> games, AsyncCallback<ArrayList<FullGame>> callback) {
        new SyncFullGamesTask(games, getTaskFinishedListener(callback)).start();
    }

    public void setGamesToSync() {

    }

    public void addGameToSync(FullGame game) {
        gamesToSync.add(game);
    }

    public void clearGamesToSync() {
        gamesToSync.clear();
    }
}
