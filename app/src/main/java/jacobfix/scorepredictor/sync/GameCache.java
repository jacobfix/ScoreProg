package jacobfix.scorepredictor.sync;

import java.util.Collection;
import java.util.HashSet;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.OriginalSyncGamesTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;

public class GameCache extends SyncableCache<String, NflGame> {

    private static final String TAG = GameCache.class.getSimpleName();

    private HashSet<NflGame> gamesToSync = new HashSet<>();

    public GameCache() {
        syncTask = new OriginalSyncGamesTask(gamesToSync, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    notifyAllOfFailure(task.getError());
                    return;
                }

                NflGame[] result = (NflGame[]) task.getResult();
                for (NflGame game : result)
                    set(game.getGameId(), game);
                notifyAllOfSuccess(result);
            }
        });
    }

    public void sync(Collection<NflGame> games, final AsyncCallback<NflGame[]> listener) {
        new OriginalSyncGamesTask(games, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    listener.onFailure(task.getError());
                    return;
                }

                NflGame[] result = (NflGame[]) task.getResult();
                for (NflGame game : result)
                    set(game.getGameId(), game);
                listener.onSuccess(result);
            }
        }).start();
    }
}
