package jacobfix.scoreprog.task;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import jacobfix.scoreprog.FullGame;
import jacobfix.scoreprog.server.GameServerInterface;
import jacobfix.scoreprog.server.JsonParser;

public class SyncFullGamesTask extends BaseTask<Collection<FullGame>> {

    private static final String TAG = SyncFullGamesTask.class.getSimpleName();

    private Collection<FullGame> games;

    public SyncFullGamesTask(Collection<FullGame> games, TaskFinishedListener listener) {
        super(listener);
        this.games = games;
    }

    @Override
    public void execute() {
        try {
            HashSet<FullGame> games = new HashSet<>(this.games);
            Collection<FullGame> result = new ArrayList<>();

            for (FullGame game : games) {
                if (game.getStartTime() > System.currentTimeMillis()) {
                    /* Full game JSON will not exist, so don't try to get it. */
                    Log.d(TAG, "Start time: " + game.getStartTime() + ", Now: " + System.currentTimeMillis());
                    Log.d(TAG, "XSW (" + game.getId() + ") Not syncing because the game has not begun");
                    result.add(game);
                    continue;
                }

                Log.d(TAG, "XSW (" + game.getId() + ") Game start time has been reached. Syncing");
                JSONObject json = GameServerInterface.getDefault().getFullGameJson(game.getId());
                Log.d(TAG, "About to update game");
                JsonParser.updateFullGame(game, json);
                result.add(game);
            }
            setResult(result);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
