package jacobfix.scorepredictor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.sync.FullGameCache;

public class GameProvider {

    private static final String TAG = GameProvider.class.getSimpleName();

    public static FullGameCache fullGameCache = new FullGameCache();

    // public static GameSyncableMap gameCache = new GameSyncableMap();

    public static boolean getFullGame(AtomicGame atom, final AsyncCallback<FullGame> callback) {
        FullGame cachedGame = fullGameCache.get(atom.getId());
        if (cachedGame != null) {
            callback.onSuccess(cachedGame);
            return true;
        }
        fullGameCache.sync(new FullGame(atom), callback);
        return false;
    }

    public static boolean getFullGames(Collection<AtomicGame> atoms, final AsyncCallback<Map<String, FullGame>> callback) {
        final Map<String, FullGame> retrieved = new HashMap<>();
        LinkedList<FullGame> needed = new LinkedList<>();

        for (AtomicGame atom : atoms) {
            FullGame cachedGame = fullGameCache.get(atom.getId());
            if (cachedGame != null) {
                retrieved.put(cachedGame.getId(), cachedGame);
            } else {
                needed.add(new FullGame(atom));
            }
        }

        if (needed.isEmpty()) {
            callback.onSuccess(retrieved);
            return true;
        }

        fullGameCache.sync(needed, new AsyncCallback<ArrayList<FullGame>>() {
            @Override
            public void onSuccess(ArrayList<FullGame> result) {
                for (FullGame game : result)
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

    public static void addGameToSync(AtomicGame game) {
        FullGame cachedGame = fullGameCache.get(game.getId());
        if (cachedGame == null)
            cachedGame = new FullGame(game);
        fullGameCache.addGameToSync(cachedGame);
    }

    public static void setGamesToSync(Collection<AtomicGame> games) {
        fullGameCache.clearGamesToSync();
        for (AtomicGame game : games)
            addGameToSync(game);
    }

    public static void setGameToSync(AtomicGame game) {
        setGamesToSync(Collections.singletonList(game));
    }

    public static void registerSyncCallback(AsyncCallback callback) {
        fullGameCache.registerSyncListener(callback);
    }

    public static void unregisterSyncCallback(AsyncCallback callback) {
        fullGameCache.unregisterSyncListener(callback);
    }
}
