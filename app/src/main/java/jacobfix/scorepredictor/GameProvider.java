package jacobfix.scorepredictor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import jacobfix.scorepredictor.sync.FullGameCache;

public class GameProvider {

    private static final String TAG = GameProvider.class.getSimpleName();

    public static FullGameCache fullGameCache = new FullGameCache();

    // public static GameSyncableMap gameCache = new GameSyncableMap();

    public static boolean getFullGame(Game game, final AsyncCallback<FullGame> callback) {
        FullGame cachedGame = fullGameCache.get(game.getId());
        if (cachedGame != null) {
            callback.onSuccess(cachedGame);
            return true;
        }
        fullGameCache.sync(new FullGame(game), callback);
        return false;
    }

    public static boolean getFullGames(Collection<Game> games, final AsyncCallback<Map<String, FullGame>> callback) {
        return false;
    }

    /*
    public static boolean getFullGame(AtomicGame atom, final AsyncCallback<OriginalFullGame> callback) {
        OriginalFullGame cachedGame = fullGameCache.get(atom.getId());
        if (cachedGame != null) {
            callback.onSuccess(cachedGame);
            return true;
        }
        fullGameCache.sync(new OriginalFullGame(atom), callback);
        return false;
    }
    */

    /*
    public static boolean getFullGames(Collection<AtomicGame> atoms, final AsyncCallback<Map<String, OriginalFullGame>> callback) {
        final Map<String, OriginalFullGame> retrieved = new HashMap<>();
        LinkedList<OriginalFullGame> needed = new LinkedList<>();

        for (AtomicGame atom : atoms) {
            OriginalFullGame cachedGame = fullGameCache.get(atom.getId());
            if (cachedGame != null) {
                retrieved.put(cachedGame.getId(), cachedGame);
            } else {
                needed.add(new OriginalFullGame(atom));
            }
        }

        if (needed.isEmpty()) {
            callback.onSuccess(retrieved);
            return true;
        }

        fullGameCache.sync(needed, new AsyncCallback<ArrayList<OriginalFullGame>>() {
            @Override
            public void onSuccess(ArrayList<OriginalFullGame> result) {
                for (OriginalFullGame game : result)
                    retrieved.put(game.getId(), game);
                callback.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
        return false;
    }
    */

    public static void addGameToSync(Game game) {
        if (game instanceof FullGame) {
            fullGameCache.addGameToSync((FullGame) game);
        } else {
            FullGame cachedGame = fullGameCache.get(game.getId());
            if (cachedGame == null)
                cachedGame = new FullGame(game);
            fullGameCache.addGameToSync(cachedGame);
        }
    }

    public static void setGamesToSync(Collection<Game> games) {
        fullGameCache.clearGamesToSync();
        for (Game game : games)
            addGameToSync(game);
    }

    public static void registerSyncCallback(AsyncCallback callback) {
        fullGameCache.registerSyncListener(callback);
    }

    public static void unregisterSyncCallback(AsyncCallback callback) {
        fullGameCache.unregisterSyncListener(callback);
    }
}
