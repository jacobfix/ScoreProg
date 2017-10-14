package jacobfix.scorepredictor.sync;

import java.util.Collection;
import java.util.Collections;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.schedule.ScheduledGame;

public class OriginalGameProvider {

    private static final ActiveGameCache activeGamesCache = new ActiveGameCache();
    private static final GameCache otherGamesCache = new GameCache();

    public static Collection<NflGame> getActiveGames() {
        return activeGamesCache.getAll();
    }

    public static NflGame optActiveGame(String gameId) {
        return activeGamesCache.get(gameId);
    }

    public static void createGame(ScheduledGame scheduled) {
        NflGame game = new NflGame(scheduled.gid);
        otherGamesCache.set(game.getGameId(), game);
    }

    public static NflGame optGame(String gameId) {
        NflGame game = activeGamesCache.get(gameId);
        if (game == null)
            game = otherGamesCache.get(gameId);
        return game;
    }

    public static boolean getGame(String gameId, final AsyncCallback<NflGame[]> listener) {
        NflGame game = optGame(gameId);
        if (game == null && (game = otherGamesCache.get(gameId)) == null) {
            otherGamesCache.sync(Collections.singletonList(game), listener);
            return false;
        } else {
            listener.onSuccess(new NflGame[]{game});
            return true;
        }
    }

    public static void registerActiveGamesSyncListener(AsyncCallback<NflGame[]> listener) {
        activeGamesCache.registerSyncListener(listener);
    }

    public static void unregisterActiveGamesSyncListener(AsyncCallback<NflGame[]> listener) {
        activeGamesCache.unregisterSyncListener(listener);
    }

    public static void registerOtherGamesSyncListener(AsyncCallback<NflGame[]> listener) {
        otherGamesCache.registerSyncListener(listener);
    }

    public static void unregisterOtherGamesSyncListener(AsyncCallback<NflGame[]> listener) {
        otherGamesCache.unregisterSyncListener(listener);
    }
}
