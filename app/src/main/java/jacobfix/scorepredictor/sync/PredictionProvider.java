package jacobfix.scorepredictor.sync;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.Predictions;

public class PredictionProvider {

    private static final String TAG = PredictionProvider.class.getSimpleName();

    private static PredictionCache predictionCache = new PredictionCache();

    private static HashSet<String> userIdsToSync = new HashSet<>();
    private static HashSet<String> gameIdsToSync = new HashSet<>();

    public static boolean getPrediction(final String userId, String gameId, final AsyncCallback<Prediction> callback) {
        return getPredictions(Collections.singletonList(userId), Collections.singletonList(gameId), true, new AsyncCallback<Predictions>() {
            @Override
            public void onSuccess(Predictions result) {
                Prediction prediction = (result.get(userId) != null) ? result.get(userId).getFirst() : null;
                callback.onSuccess(prediction);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public static boolean getPredictions(Collection<String> userIds, Collection<String> gameIds, boolean indexByUser, final AsyncCallback<Predictions> callback) {
        final Predictions retrieved = new Predictions(indexByUser);
        LinkedList<Prediction> needed = new LinkedList<>();

        for (String userId : userIds) {
            for (String gameId : gameIds) {
                Prediction cachedPrediction = predictionCache.get(userId, gameId);

                if (cachedPrediction != null) {
                    Log.d(TAG, String.format("Prediction %s,%s hit in cache", userId, gameId));
                    retrieved.put(cachedPrediction);
                } else {
                    Log.d(TAG, String.format("Prediction %s,%s DID NOT hit in cache", userId, gameId));
                    needed.add(new Prediction(userId, gameId));
                }
            }
        }

        if (needed.isEmpty()) {
            callback.onSuccess(retrieved);
            return true;
        }

        predictionCache.sync(needed, new AsyncCallback<ArrayList<Prediction>>() {
            @Override
            public void onSuccess(ArrayList<Prediction> result) {
                retrieved.putAll(result);
                callback.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
        return false;
    }

    public static boolean getPredictions(Collection<String> userIds, String gameId, final AsyncCallback<Map<String, Prediction>> callback) {
        final Map<String, Prediction> retrieved = new HashMap<>();
        LinkedList<Prediction> needed = new LinkedList<>();

        for (String userId : userIds) {
            Prediction cachedPrediction = predictionCache.get(userId, gameId);
            if (cachedPrediction != null) {
                Log.d(TAG, String.format("Prediction %s,%s hit in cache", userId, gameId));
                retrieved.put(userId, cachedPrediction);
            } else {
                Log.d(TAG, String.format("Prediction %s,%s DID NOT hit in cache", userId, gameId));
                needed.add(new Prediction(userId, gameId));
            }
        }

        if (needed.isEmpty()) {
            callback.onSuccess(retrieved);
            return true;
        }

        predictionCache.sync(needed, new AsyncCallback<ArrayList<Prediction>>() {
            @Override
            public void onSuccess(ArrayList<Prediction> result) {
                for (Prediction p : result) {
                    retrieved.put(p.getUserId(), p);
                }
                callback.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
        return false;
    }

    public static boolean getPredictions(String userId, Collection<String> gameIds, final AsyncCallback<Map<String, Prediction>> callback) {
        final Map<String, Prediction> retrieved = new HashMap<>();
        LinkedList<Prediction> needed = new LinkedList<>();

        for (String gameId : gameIds) {
            Prediction cachedPrediction = predictionCache.get(userId, gameId);
            if (cachedPrediction != null) {
                Log.d(TAG, String.format("Prediction %s,%s hit in cache", userId, gameId));
                retrieved.put(gameId, cachedPrediction);
            } else {
                Log.d(TAG, String.format("Prediction %s,%s DID NOT hit in cache", userId, gameId));
                needed.add(new Prediction(userId, gameId));
            }
        }

        if (needed.isEmpty()) {
            callback.onSuccess(retrieved);
            return true;
        }

        predictionCache.sync(needed, new AsyncCallback<ArrayList<Prediction>>() {
            @Override
            public void onSuccess(ArrayList<Prediction> result) {
                for (Prediction p : result) {
                    retrieved.put(p.getGameId(), p);
                }
                callback.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
        return false;
    }

    public static void registerSyncCallback(AsyncCallback<ArrayList<Prediction>> callback) {
        predictionCache.registerSyncListener(callback);
    }

    public static void unregisterSyncCallback(AsyncCallback<ArrayList<Prediction>> callback) {
        predictionCache.unregisterSyncListener(callback);
    }

    public static void setPredictionsToSync(Collection<String> userIds, Collection<String> gameIds) {
        clearPredictionsToSync();
        for (String userId : userIds) {
            for (String gameId : gameIds) {
                addPredictionToSync(userId, gameId);
            }
        }
    }

    public static void addPredictionToSync(String userId, String gameId) {
        Prediction cachedPrediction = predictionCache.get(userId, gameId);
        if (cachedPrediction == null)
            cachedPrediction = new Prediction(userId, gameId);
        predictionCache.addPredictionToSync(cachedPrediction);
    }

    public static void clearPredictionsToSync() {
        predictionCache.clearPredictionsToSync();
    }

    public static void setUserIdsToSync(Collection<String> userIds) {
        userIdsToSync.clear();
        userIdsToSync.addAll(userIds);
    }

    public static void setGameIdsToSync(Collection<String> gameIds) {
        gameIdsToSync.clear();
        gameIdsToSync.addAll(gameIds);
    }

    public static void setUserIdToSync(String userId) {
        userIdsToSync.clear();
        userIdsToSync.add(userId);
    }

    public static void setGameIdToSync(String gameId) {
        gameIdsToSync.clear();
        gameIdsToSync.add(gameId);
    }
}
