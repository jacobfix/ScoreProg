package jacobfix.scoreprog.sync;

import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.DriveFeed;
import jacobfix.scoreprog.FullGame;
import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.SyncGamesTask;
import jacobfix.scoreprog.task.TaskFinishedListener;

public class GameProvider {

    private static final String TAG = GameProvider.class.getSimpleName();

    private static HashMap<String, Game> allGames = new HashMap<>();
    private static LRUCache<String, DriveFeed> driveFeedCache = new LRUCache<>();

    private static Set<GameAndDriveFeed> gamesToSync = new HashSet<>();
    private static SyncGamesTask syncGamesTask = new SyncGamesTask(gamesToSync, SyncGamesTask.FLAG_SKIP_FINAL_GAMES, new TaskFinishedListener<SyncGamesTask>() {
        @Override
        public void onTaskFinished(SyncGamesTask task) {
            if (task.errorOccurred()) {
                gameSyncHandler.notifyAllOfFailure(task.getError());
                return;
            }

            Collection<GameAndDriveFeed> result = task.getResult();
            for (GameAndDriveFeed pair : result)
                driveFeedCache.put(pair.game.getId(), pair.driveFeed);

            gameSyncHandler.notifyAllOfSuccess(task.getResult());
        }
    });
    private static ScheduledSyncHandler<GameAndDriveFeed> gameSyncHandler = new ScheduledSyncHandler<>(syncGamesTask, 0, 60);

    public static Game getGame(String gameId) {
        return allGames.get(gameId);
    }

    public static boolean getDriveFeed(String gameId, final AsyncCallback<DriveFeed> callback) {
        DriveFeed driveFeed = driveFeedCache.get(gameId);
        if (driveFeed != null) {
            callback.onSuccess(driveFeed);
            return true;
        }

        syncGame(gameId, new AsyncCallback<GameAndDriveFeed>() {
            @Override
            public void onSuccess(GameAndDriveFeed result) {
                callback.onSuccess(result.driveFeed);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
        return false;
    }

    public static void putGame(Game game) {
        allGames.put(game.getId(), game);
    }

    public static void syncGame(String gameId, final AsyncCallback<GameAndDriveFeed> callback) {
        syncGames(Collections.singletonList(gameId), new AsyncCallback<Collection<GameAndDriveFeed>>() {
            @Override
            public void onSuccess(Collection<GameAndDriveFeed> result) {
                callback.onSuccess(result.toArray(new GameAndDriveFeed[1])[0]);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public static void syncGames(Collection<String> gameIds, final AsyncCallback<Collection<GameAndDriveFeed>> callback) {
        getSyncGamesTask(gameIds, callback).start();
    }

    public static void syncGamesInForeground(Collection<String> gameIds) {
        getSyncGamesTask(gameIds, new AsyncCallback<Collection<GameAndDriveFeed>>() {
            @Override
            public void onSuccess(Collection<GameAndDriveFeed> result) {
                // Do nothing
            }

            @Override
            public void onFailure(Exception e) {
                // Do nothing
            }
        }).startOnThisThread();
    }

    private static SyncGamesTask getSyncGamesTask(Collection<String> gameIds, final AsyncCallback<Collection<GameAndDriveFeed>> callback) {
        Set<GameAndDriveFeed> pairs = new HashSet<>();
        for (String gameId : gameIds) {
            Game game = allGames.get(gameId);
            DriveFeed driveFeed = driveFeedCache.get(game.getId());
            if (driveFeed == null)
                driveFeed = new DriveFeed();
            pairs.add(new GameAndDriveFeed(game, driveFeed));
        }

        // TODO: Add option to sync just the games, not the DriveFeeds too
        return new SyncGamesTask(pairs, new TaskFinishedListener<SyncGamesTask>() {
            @Override
            public void onTaskFinished(SyncGamesTask task) {
                if (task.errorOccurred()) {
                    callback.onFailure(task.getError());
                    return;
                }

                Collection<GameAndDriveFeed> result = task.getResult();
                for (GameAndDriveFeed pair : result)
                    driveFeedCache.put(pair.game.getId(), pair.driveFeed);

                callback.onSuccess(result);
            }
        });
    }

    public static void addGameToSync(String gameId) {
        DriveFeed driveFeed = driveFeedCache.get(gameId);
        if (driveFeed == null)
            driveFeed = new DriveFeed();
        gamesToSync.add(new GameAndDriveFeed(allGames.get(gameId), driveFeed));
        Log.d(TAG, "Added: " + gameId);
    }

    public static void addGamesToSync(Collection<String> gameIds) {
        for (String gameId : gameIds)
            addGameToSync(gameId);
    }

    public static void setGamesToSync(Collection<String> gameIds) {
        Set<GameAndDriveFeed> pairs = new HashSet<>();
        for (String gameId : gameIds) {
            DriveFeed driveFeed = driveFeedCache.get(gameId);
            if (driveFeed == null)
                driveFeed = new DriveFeed();
            pairs.add(new GameAndDriveFeed(allGames.get(gameId), driveFeed));
        }
        gamesToSync.clear();
        gamesToSync.addAll(pairs);
    }

    public static void registerGameSyncListener(AsyncCallback<Collection<GameAndDriveFeed>> callback) {
        gameSyncHandler.registerSyncListener(callback);
    }

    public static void unregisterGameSyncListener(AsyncCallback<Collection<GameAndDriveFeed>> callback) {
        gameSyncHandler.unregisterSyncListener(callback);
    }

//    public static boolean getFullGame(Game game, final AsyncCallback<FullGame> callback) {
//        FullGame cachedGame = fullGameCache.get(game.getId());
//        if (cachedGame != null) {
//            callback.onSuccess(cachedGame);
//            return true;
//        }
//        Log.d(TAG, "GameProvider home: " + game.getAwayAbbr() + ", away: " + game.getHomeAbbr());
//        fullGameCache.sync(new FullGame(game), callback);
//        return false;
//    }
//
//    public static boolean getFullGames(Collection<Game> games, final AsyncCallback<Map<String, FullGame>> callback) {
//        return false;
//    }

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

//    public static void addGameToSync(Game game) {
//        if (game instanceof FullGame) {
//            fullGameCache.addGameToSync((FullGame) game);
//        } else {
//            FullGame cachedGame = fullGameCache.get(game.getId());
//            if (cachedGame == null)
//                cachedGame = new FullGame(game);
//            fullGameCache.addGameToSync(cachedGame);
//        }
//    }
//
//    public static void setGamesToSync(Collection<Game> games) {
//        fullGameCache.clearGamesToSync();
//        for (Game game : games)
//            addGameToSync(game);
//    }

//    public static void addFullGameToSync(FullGame game) {
//        fullGameCache.addFullGameToSync(game);
//    }
//
//    public static void setFullGamesToSync(Collection<FullGame> games) {
//        fullGameCache.setFullGamesToSync(games);
//    }

//    public static void addGameToSync(Game game) {
//        if (game instanceof FullGame) {
//            fullGameCache.addFullGameToSync((FullGame) game);
//        } else {
//            FullGame fullGame = fullGameCache.get(game.getId());
//            if (fullGame == null)
//                fullGame = new FullGame(game);
//            fullGameCache.addFullGameToSync(fullGame);
//        }
//    }
//
//    public static void setGamesToSync(Collection<Game> games) {
//        fullGameCache.clearFullGamesToSync();
//        for (Game game : games)
//            addGameToSync(game);
//    }
//
//    public static void registerFullGameSyncCallback(AsyncCallback<Collection<FullGame>> callback) {
//        fullGameCache.registerSyncListener(callback);
//    }
//
//    public static void unregisterFullGameSyncCallback(AsyncCallback<Collection<FullGame>> callback) {
//        fullGameCache.unregisterSyncListener(callback);
//    }
}
