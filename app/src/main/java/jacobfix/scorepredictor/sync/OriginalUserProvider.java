package jacobfix.scorepredictor.sync;

import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.OriginalPredictions;
import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.users.User;

public class OriginalUserProvider {

    private static final String TAG = OriginalUserProvider.class.getSimpleName();

    private static final DetailsCache detailsCache = new DetailsCache();
    private static final PredictionsCache predictionsCache = new PredictionsCache();

    public static boolean getUserDetails(Collection<String> ids, final AsyncCallback<Map<String, User>> listener) {
        final HashMap<String, User> retrieved = new HashMap<>();
        final LinkedList<User> needed = new LinkedList<>();

        for (String id : ids) {
            User cachedUser = detailsCache.get(id);
            if (cachedUser != null)
                retrieved.put(id, cachedUser);
            else
                needed.add(new User(id));
        }

        Log.d(TAG, "Hit in the cache: " + retrieved.toString());
        if (needed.isEmpty()) {
            Log.d(TAG, "All hit in the cache");
            listener.onSuccess(retrieved);
            return true;
        }
        Log.d(TAG, "Not all hit in the cache. Retrieving now...");

        detailsCache.sync(needed, new AsyncCallback<User[]>() {
            @Override
            public void onSuccess(User[] users) {
                for (User user : users)
                    retrieved.put(user.getId(), user);
                listener.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception exception) {
                listener.onFailure(exception);
            }
        });

        return false;
    }

    public static boolean getFriends(String uid, final AsyncCallback<Collection<String>> callback) {
        LinkedList<String> friends = new LinkedList<>();
        for (int i = 0; i < 10; i++)
            friends.add(String.valueOf(i));
        callback.onSuccess(friends);
        return true;
    }

    public static boolean getUserPredictions(Collection<String> gids, String uid, final AsyncCallback<Map<String, OriginalPredictions>> listener) {
        final HashMap<String, OriginalPredictions> retrieved = new HashMap<>();
        final LinkedList<String> needed = new LinkedList<>();

        OriginalPredictions predictions = predictionsCache.get(uid);
        if (predictions != null) {
            for (String gid : gids) {
                if (predictions.get(gid) == null) {
                    needed.add(gid);
                }
            }
        } else {
            predictions = new OriginalPredictions(uid);
            needed.addAll(gids);
        }

        if (needed.isEmpty()) {
            retrieved.put(uid, predictions);
            listener.onSuccess(retrieved);
            return true;
        }

        syncPredictionsCache(needed, Collections.singletonList(predictions), retrieved, listener);
        return false;
    }

    public static boolean getUserPredictions(String gid, Collection<String> uids, final AsyncCallback<Map<String, OriginalPredictions>> listener) {
        final HashMap<String, OriginalPredictions> retrieved = new HashMap<>();
        final LinkedList<OriginalPredictions> needed = new LinkedList<>();

        for (String uid : uids) {
            OriginalPredictions cachedPredictions = predictionsCache.get(uid);
            if (cachedPredictions != null) {
                if (cachedPredictions.get(gid) != null) {
                    retrieved.put(uid, cachedPredictions);
                } else {
                    needed.add(cachedPredictions);
                }
            } else {
                needed.add(new OriginalPredictions(uid));
            }
        }

        if (needed.isEmpty()) {
            listener.onSuccess(retrieved);
            return true;
        }

        syncPredictionsCache(Collections.singletonList(gid), needed, retrieved, listener);
        return false;
    }

    public static void getInvitees(String gameId, AsyncCallback<LinkedList<String>> callback) {

    }

    private static void syncPredictionsCache(Collection<String> gids, Collection<OriginalPredictions> predictions, final Map<String, OriginalPredictions> toReturn, final AsyncCallback<Map<String, OriginalPredictions>> callback) {
        predictionsCache.sync(gids, predictions, new AsyncCallback<OriginalPredictions[]>() {
            @Override
            public void onSuccess(OriginalPredictions[] result) {
                for (OriginalPredictions r : result)
                    toReturn.put(r.getId(), r);
                callback.onSuccess(toReturn);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public static void addDetailsToSync(String uid) {
        User cachedUser = detailsCache.get(uid);
        if (cachedUser != null)
            detailsCache.addDetailsToSync(cachedUser);
        else
            detailsCache.addDetailsToSync(new User(uid));
    }

    public static void setDetailsToSync(Collection<String> uids) {
        detailsCache.clearDetailsToSync();
        for (String uid : uids)
            addDetailsToSync(uid);
    }

    public static void addGameToPredictionsSync(String game) {
        predictionsCache.addGameToSync(game);
    }

    public static void setGameToPredictionsSync(Collection<String> games) {
        predictionsCache.clearGamesToSync();
        predictionsCache.setGamesToSync(games);
    }

    public static void addPredictionsToSync(String uid) {
        OriginalPredictions cachedPredictions = predictionsCache.get(uid);
        if (cachedPredictions != null)
            predictionsCache.addPredictionsToSync(cachedPredictions);
        else
            predictionsCache.addPredictionsToSync(new OriginalPredictions(uid));
    }

    public static void setPredictionsToSync(Collection<String> uids) {
        predictionsCache.clearPredictionsToSync();
        for (String uid : uids)
            addPredictionsToSync(uid);
    }

    public static void registerDetailsSyncListener(AsyncCallback<User[]> listener) {
        detailsCache.registerSyncListener(listener);
    }

    public static void registerPredictionsSyncListener(AsyncCallback<OriginalPredictions[]> listener) {
        predictionsCache.registerSyncListener(listener);
    }

    public static void unregisterDetailsSyncListener(AsyncCallback<User[]> listener) {
        detailsCache.unregisterSyncListener(listener);
    }

    public static void unregisterPredictionsSyncListener(AsyncCallback<OriginalPredictions[]> listener) {
        predictionsCache.unregisterSyncListener(listener);
    }
}
