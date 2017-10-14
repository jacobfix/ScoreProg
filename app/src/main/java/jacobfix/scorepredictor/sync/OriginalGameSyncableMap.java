package jacobfix.scorepredictor.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.GameRetriever;
import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.ScheduleRetriever;
import jacobfix.scorepredictor.schedule.ScheduledGame;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;

public class OriginalGameSyncableMap { //extends SyncableMap<String, NflGame> {

    private LinkedList<GameSyncObject> syncObjects = new LinkedList<>();

    public OriginalGameSyncableMap() {
        /* syncTask = new SyncGamesTask(syncObjects, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                // if (task.errorOccurred()) notifyAllOfFailure(task.getError());
                // else                      notifyAllOfSuccess((Collection<NflGame>) task.getResult());
            }
        });*/
    }

    public void sync(int year, int week, int seasonType, boolean full, final AsyncCallback<NflGame[]> callback) {
        GameSyncObject object = new GameSyncObject(year, new int[]{week}, seasonType, full);
        new SyncGamesTask(Collections.singletonList(object), new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) callback.onFailure(task.getError());
                else                      callback.onSuccess((NflGame[]) task.getResult());
            }
        }).start();
    }

    public NflGame[] syncInForeground(int year, int week, int seasonType, boolean full) {
        GameSyncObject object = new GameSyncObject(year, new int[]{week}, seasonType, full);
        SyncGamesTask syncGamesTask = new SyncGamesTask(Collections.singletonList(object), null);
        syncGamesTask.startOnThisThread();
        return syncGamesTask.getResult();
    }

    class SyncGamesTask extends BaseTask<NflGame[]> {

        Collection<GameSyncObject> syncObjects;

        public SyncGamesTask(Collection<GameSyncObject> syncObjects, TaskFinishedListener listener) {
            super(listener);
            this.syncObjects = syncObjects;
        }

        @Override
        public void execute() {
            try {
                ArrayList<NflGame> result = new ArrayList<>();
                for (GameSyncObject syncable : syncObjects) {
                    for (int week : syncable.weeks) {
                        ScheduledGame[] scheduledGames = ScheduleRetriever.get().getWeek(syncable.season, week, syncable.seasonType);
                        for (ScheduledGame scheduledGame : scheduledGames) {
                            // NflGame game = (NflGame) get(scheduledGame.gid);
                            NflGame game = null;

                            boolean newGame = false;
                            if (game == null) {
                                newGame = true;
                                game = new NflGame();
                            }

                            game.syncScheduleDetails(scheduledGame);

                            if (syncable.full)
                                game.syncFullDetails(GameRetriever.getGameJson(game.getGameId()));

                            // if (newGame)
                            //     put(game.getGameId(), game);

                            result.add(game);
                        }
                    }
                }
                setResult(result.toArray(new NflGame[result.size()]));
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    class GameSyncObject {

        public int season;
        public int[] weeks;
        public int seasonType;
        public boolean full;

        public GameSyncObject(int season, int[] weeks, int seasonType, boolean full) {
            this.season = season;
            this.weeks = weeks;
            this.seasonType = seasonType;
            this.full = full;
        }
    }
}
