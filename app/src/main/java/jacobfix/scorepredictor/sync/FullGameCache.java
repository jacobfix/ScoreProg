package jacobfix.scorepredictor.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.FullGame;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SyncFullGamesTask;

public class FullGameCache extends SyncableMap<String, FullGame> {

    private HashSet<FullGame> gamesToSync = new HashSet<>();

    @Override
    public String key(FullGame game) {
        return game.getId();
    }

    @Override
    public SyncFullGamesTask getSyncTask() {
        return new SyncFullGamesTask(gamesToSync, getTaskFinishedListener(scheduledSyncCallback));
    }

    public void sync(FullGame game, final AsyncCallback<FullGame> callback) {
        sync(Collections.singletonList(game), new AsyncCallback<ArrayList<FullGame>>() {
            @Override
            public void onSuccess(ArrayList<FullGame> result) {
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

    public void setGamesToSync(Collection<FullGame> games) {
        gamesToSync.clear();
        gamesToSync.addAll(games);
    }

    public void addGameToSync(FullGame game) {
        gamesToSync.add(game);
    }

    public void clearGamesToSync() {
        gamesToSync.clear();
    }

}
