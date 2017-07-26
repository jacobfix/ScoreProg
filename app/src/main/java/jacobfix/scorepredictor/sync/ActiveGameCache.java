package jacobfix.scorepredictor.sync;

import java.util.LinkedList;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.GetActiveGamesTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;

public class ActiveGameCache extends GameCache {

    public ActiveGameCache() {
        syncTask = new GetActiveGamesTask(new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    notifyAllOfFailure(task.getError());
                    return;
                }

                String[] gameIds = (String[]) task.getResult();
                LinkedList<NflGame> activeGames = new LinkedList<>();
                for (String id : gameIds) {
                    NflGame cachedGame = get(id);
                    if (cachedGame != null)
                        activeGames.add(cachedGame);
                    else
                        activeGames.add(new NflGame(id));
                }

                sync(activeGames, new AsyncCallback<NflGame[]>() {
                    @Override
                    public void onSuccess(NflGame[] result) {
                        notifyAllOfSuccess(result);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        notifyAllOfFailure(e);
                    }
                });
            }
        });
    }
}
