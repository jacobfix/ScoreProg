package jacobfix.scorepredictor.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.schedule.ScheduleRetriever;

public class SyncGamesTask extends BaseTask {

    private Collection<NflGame> games;

    public SyncGamesTask(Collection<NflGame> games, TaskFinishedListener listener) {
        this.games = games;
    }

    @Override
    public void execute() {

    }
}
