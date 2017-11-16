package jacobfix.scorepredictor.task;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import jacobfix.scorepredictor.FullGame;
import jacobfix.scorepredictor.server.GameServerInterface;

public class SyncFullGamesTask extends BaseTask<ArrayList<FullGame>> {

    private static final String TAG = SyncFullGamesTask.class.getSimpleName();

    private HashSet<FullGame> games;

    public SyncFullGamesTask(Collection<FullGame> games, TaskFinishedListener listener) {
        super(listener);
        Log.d(TAG, games.toString());
        this.games = new HashSet<>(games);
    }

    @Override
    public void execute() {
        try {
            Thread.sleep(4000);
            ArrayList<FullGame> result = new ArrayList<>();
            for (FullGame game : games) {
                JSONObject json = GameServerInterface.getDefault().getFullGameJson(game.getId());
                if (json != null) {
                    /* Update method has internal synchronization. */
                    game.updateFromFullJson(json);
                }
                result.add(game);
            }
            setResult(result);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
