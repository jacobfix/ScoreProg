package jacobfix.scoreprog.task;

import android.util.Log;

import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;

import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.server.GameServerInterface;
import jacobfix.scoreprog.sync.GameAndDriveFeed;

public class SyncGamesTask extends BaseTask<Collection<GameAndDriveFeed>> {

    private static final String TAG = SyncGamesTask.class.getSimpleName();

    private Collection<GameAndDriveFeed> pairs;
    private int flags = 0;

    public static final int FLAG_UPDATE_GAMES_ONLY = 0x1;
    public static final int FLAG_SKIP_FINAL_GAMES = 0x2;

    public SyncGamesTask(Collection<GameAndDriveFeed> pairs, TaskFinishedListener listener) {
        this(pairs, 0, listener);
    }

    public SyncGamesTask(Collection<GameAndDriveFeed> pairs, int flags, TaskFinishedListener listener) {
        super(listener);
        this.pairs = pairs;
        this.flags = flags;
    }

    @Override
    public void execute() {
        try {
            Collection<GameAndDriveFeed> toSync = new HashSet<>(this.pairs);
            for (GameAndDriveFeed pair : toSync) {
                if (pair.game.getStartTime() > System.currentTimeMillis())
                    continue;

                if ((flags & FLAG_SKIP_FINAL_GAMES) > 0 && pair.game.isFinal())
                    continue;

                JSONObject json = GameServerInterface.getDefault().getGameJson(pair.game.getId());

                if ((flags & FLAG_UPDATE_GAMES_ONLY) > 0 || pair.driveFeed == null) {
                    GameServerInterface.getDefault().updateGameOnly(pair.game, json);
                } else {
                    GameServerInterface.getDefault().updateGameAndDriveFeed(pair, json);
                }
            }
            setResult(toSync);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
