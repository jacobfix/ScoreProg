package jacobfix.scoreprog.sync;

import android.provider.Telephony;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.LocalAccountManager;
import jacobfix.scoreprog.Participants;
import jacobfix.scoreprog.Prediction;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.SyncParticipantsTask;
import jacobfix.scoreprog.task.TaskFinishedListener;

public class PredictionProvider {

    private static final String TAG = PredictionProvider.class.getSimpleName();

    private static PredictionCache predictionCache = new PredictionCache();
    private static LRUCache<String, Participants> participantsCache = new LRUCache<>();

    private static Set<Participants> participantsToSync = new HashSet<>();
    private static SyncParticipantsTask syncParticipantsTask = new SyncParticipantsTask(participantsToSync, new TaskFinishedListener<SyncParticipantsTask>() {
        @Override
        public void onTaskFinished(SyncParticipantsTask task) {
            if (task.errorOccurred()) {
                participantsSyncHandler.notifyAllOfFailure(task.getError());
                return;
            }

            Collection<Participants> result = task.getResult();
            for (Participants participants : result)
                participantsCache.put(participants.getGameId(), participants);

            participantsSyncHandler.notifyAllOfSuccess(result);
        }
    });
    private static ScheduledSyncHandler<Participants> participantsSyncHandler = new ScheduledSyncHandler<>(syncParticipantsTask, 0, 60);

    public static boolean getMyPrediction(final String gameId, final AsyncCallback<Prediction> callback) {
        return getMyPredictions(Collections.singletonList(gameId), new AsyncCallback<Map<String, Prediction>>() {
            @Override
            public void onSuccess(Map<String, Prediction> result) {
                callback.onSuccess(result.get(gameId));
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public static boolean getMyPredictions(Collection<String> gameIds, final AsyncCallback<Map<String, Prediction>> callback) {
        final Map<String, Prediction> retrieved = new HashMap<>();
        List<String> needed = new LinkedList<>();

        for (String gameId : gameIds) {
            Prediction cached = predictionCache.get(LocalAccountManager.get().userId, gameId);
            if (cached != null) {
                retrieved.put(gameId, cached);
            } else {
                needed.add(gameId);
            }
        }

        if (needed.isEmpty()) {
            callback.onSuccess(retrieved);
            return true;
        }

        predictionCache.sync(needed, new AsyncCallback<Collection<Prediction>>() {
            @Override
            public void onSuccess(Collection<Prediction> result) {
                for (Prediction p : result)
                    retrieved.put(p.getGameId(), p);

                callback.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });

        return false;
    }

    public static boolean getParticipants(String gameId, AsyncCallback<Participants> callback) {
        Participants participants = participantsCache.get(gameId);
        if (participants != null) {
            callback.onSuccess(participants);
            return true;
        }

        syncParticipants(new Participants(gameId), callback);
        return false;
    }

    public static void syncParticipants(Participants participants, final AsyncCallback<Participants> callback) {
        syncParticipants(Collections.singletonList(participants), new AsyncCallback<Collection<Participants>>() {
            @Override
            public void onSuccess(Collection<Participants> result) {
                callback.onSuccess(result.toArray(new Participants[1])[0]);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public static void syncParticipants(Collection<Participants> participants, final AsyncCallback<Collection<Participants>> callback) {
        new SyncParticipantsTask(participants, new TaskFinishedListener<SyncParticipantsTask>() {
            @Override
            public void onTaskFinished(SyncParticipantsTask task) {
                if (task.errorOccurred()) {
                    callback.onFailure(task.getError());
                    return;
                }

                Collection<Participants> result = task.getResult();
                for (Participants p : result)
                    participantsCache.put(p.getGameId(), p);

                callback.onSuccess(result);
            }
        }).start();
    }

    public static void clearPredictionCache() {
        predictionCache.clear();
    }

    public static void clearParticipantsCache() {
        participantsCache.clear();
    }

//    public static boolean getPredictions(Collection<String> userIds, Collection<String> gameIds, boolean indexByUser, final AsyncCallback<Predictions> callback) {
//        final Predictions retrieved = new Predictions(indexByUser);
//        LinkedList<Prediction> needed = new LinkedList<>();
//
//        for (String userId : userIds) {
//            for (String gameId : gameIds) {
//                Prediction cachedPrediction = predictionCache.get(userId, gameId);
//
//                if (cachedPrediction != null) {
//                    Log.d(TAG, String.format("Prediction %s,%s hit in cache", userId, gameId));
//                    retrieved.put(cachedPrediction);
//                } else {
//                    Log.d(TAG, String.format("Prediction %s,%s DID NOT hit in cache", userId, gameId));
//                    needed.add(new Prediction(userId, gameId));
//                }
//            }
//        }
//
//        if (needed.isEmpty()) {
//            callback.onSuccess(retrieved);
//            return true;
//        }
//
//        predictionCache.sync(needed, new AsyncCallback<ArrayList<Prediction>>() {
//            @Override
//            public void onSuccess(ArrayList<Prediction> result) {
//                retrieved.putAll(result);
//                callback.onSuccess(retrieved);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                callback.onFailure(e);
//            }
//        });
//        return false;
//    }

//    public static boolean getPredictions(Collection<String> userIds, String gameId, final AsyncCallback<Map<String, Prediction>> callback) {
//        final Map<String, Prediction> retrieved = new HashMap<>();
//        LinkedList<Prediction> needed = new LinkedList<>();
//
//        for (String userId : userIds) {
//            Prediction cachedPrediction = predictionCache.get(userId, gameId);
//            if (cachedPrediction != null) {
//                Log.d(TAG, String.format("Prediction %s,%s hit in cache", userId, gameId));
//                retrieved.put(userId, cachedPrediction);
//            } else {
//                Log.d(TAG, String.format("Prediction %s,%s DID NOT hit in cache", userId, gameId));
//                needed.add(new Prediction(userId, gameId));
//            }
//        }
//
//        if (needed.isEmpty()) {
//            callback.onSuccess(retrieved);
//            return true;
//        }
//
//        predictionCache.sync(needed, new AsyncCallback<ArrayList<Prediction>>() {
//            @Override
//            public void onSuccess(ArrayList<Prediction> result) {
//                for (Prediction p : result) {
//                    retrieved.put(p.getUserId(), p);
//                }
//                callback.onSuccess(retrieved);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                callback.onFailure(e);
//            }
//        });
//        return false;
//    }
//
//    public static boolean getPredictions(String userId, Collection<String> gameIds, final AsyncCallback<Map<String, Prediction>> callback) {
//        final Map<String, Prediction> retrieved = new HashMap<>();
//        LinkedList<Prediction> needed = new LinkedList<>();
//
//        for (String gameId : gameIds) {
//            Prediction cachedPrediction = predictionCache.get(userId, gameId);
//            if (cachedPrediction != null) {
//                Log.d(TAG, String.format("Prediction %s,%s hit in cache", userId, gameId));
//                retrieved.put(gameId, cachedPrediction);
//            } else {
//                Log.d(TAG, String.format("Prediction %s,%s DID NOT hit in cache", userId, gameId));
//                needed.add(new Prediction(userId, gameId));
//            }
//        }
//
//        if (needed.isEmpty()) {
//            callback.onSuccess(retrieved);
//            return true;
//        }
//
//        predictionCache.sync(needed, new AsyncCallback<ArrayList<Prediction>>() {
//            @Override
//            public void onSuccess(ArrayList<Prediction> result) {
//                for (Prediction p : result) {
//                    retrieved.put(p.getGameId(), p);
//                }
//                callback.onSuccess(retrieved);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                callback.onFailure(e);
//            }
//        });
//        return false;
//    }

    public static void addGameToParticipantsSync(String gameId) {
        Participants participants = participantsCache.get(gameId);
        if (participants == null)
            participants = new Participants(gameId);
        participantsToSync.add(participants);
    }

    public static void removeGameFromParticipantsSync(String gameId) {
        for (Participants p : participantsToSync) {
            if (p.getGameId().equals(gameId)) {
                participantsToSync.remove(p);
                break;
            }
        }
    }

    public static void clearGamesInParticipantsSync() {
        participantsToSync.clear();
    }

    public static void registerParticipantsSyncListener(AsyncCallback<Collection<Participants>> callback) {
        participantsSyncHandler.registerSyncListener(callback);
    }

    public static void unregisterParticipantsSyncListener(AsyncCallback<Collection<Participants>> callback) {
        participantsSyncHandler.unregisterSyncListener(callback);
    }

//    public static void setPredictionsToSync(Collection<String> userIds, Collection<String> gameIds) {
//        clearPredictionsToSync();
//        for (String userId : userIds) {
//            for (String gameId : gameIds) {
//                addPredictionToSync(userId, gameId);
//            }
//        }
//    }

//    public static void addPredictionToSync(String userId, String gameId) {
//        Prediction cachedPrediction = predictionCache.get(userId, gameId);
//        if (cachedPrediction == null)
//            cachedPrediction = new Prediction(userId, gameId);
//        predictionCache.addPredictionToSync(cachedPrediction);
//    }
//
//    public static void clearPredictionsToSync() {
//        predictionCache.clearPredictionsToSync();
//    }
//
//    public static void setUserIdsToSync(Collection<String> userIds) {
//        userIdsToSync.clear();
//        userIdsToSync.addAll(userIds);
//    }
//
//    public static void setGameIdsToSync(Collection<String> gameIds) {
//        gameIdsToSync.clear();
//        gameIdsToSync.addAll(gameIds);
//    }
//
//    public static void setUserIdToSync(String userId) {
//        userIdsToSync.clear();
//        userIdsToSync.add(userId);
//    }
//
//    public static void setGameIdToSync(String gameId) {
//        gameIdsToSync.clear();
//        gameIdsToSync.add(gameId);
//    }
}
