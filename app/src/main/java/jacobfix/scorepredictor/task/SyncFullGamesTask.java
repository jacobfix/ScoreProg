package jacobfix.scorepredictor.task;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.FullGame;
import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.schedule.ScheduleRetriever;
import jacobfix.scorepredictor.server.GameServerInterface;

public class SyncFullGamesTask extends BaseTask<Collection<FullGame>> {

    private static final String TAG = SyncFullGamesTask.class.getSimpleName();

    private HashSet<FullGame> games;

    public SyncFullGamesTask(Collection<FullGame> games, TaskFinishedListener listener) {
        super(listener);
        this.games = new HashSet<>(games);
    }

    @Override
    public void execute() {
        ArrayList<FullGame> result = new ArrayList<>();
        try {
            for (FullGame game : games) {
                JSONObject json = GameServerInterface.getDefault().getFullGameJson(game.getId());
                if (json != null) {
                    synchronized (game) {
                        game.sync(json);
                    }
                }
                result.add(game);
            }
        } catch (Exception e) {
            reportError(e);
        } finally {
            setResult(result);
        }
    }
}
