package jacobfix.scorepredictor.sync;

import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.Predictions;
import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.users.User;

public class UserProvider {

    private static final String TAG = UserProvider.class.getSimpleName();

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

    public static boolean getUserPredictions(String gid, Collection<String> uids, final AsyncCallback<Map<String, Predictions>> listener) {
        final HashMap<String, Predictions> retrieved = new HashMap<>();
        final LinkedList<Predictions> needed = new LinkedList<>();

        for (String uid : uids) {
            Predictions cachedPredictions = predictionsCache.get(uid);
            if (cachedPredictions != null) {
                if (cachedPredictions.get(gid) != null) {
                    retrieved.put(uid, cachedPredictions);
                } else {
                    needed.add(cachedPredictions);
                }
            } else {
                needed.add(new Predictions(uid));
            }
        }

        if (needed.isEmpty()) {
            listener.onSuccess(retrieved);
            return true;
        }

        predictionsCache.sync(gid, needed, new AsyncCallback<Predictions[]>() {
            @Override
            public void onSuccess(Predictions[] predictions) {
                for (Predictions p : predictions)
                    retrieved.put(p.getId(), p);
                listener.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception exception) {
                listener.onFailure(exception);
            }
        });

        return false;
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

    public static void setGamePredictionsToSync(String gameId) {
        predictionsCache.setGameToSync(gameId);
    }

    public static void addPredictionsToSync(String uid) {
        Predictions cachedPredictions = predictionsCache.get(uid);
        if (cachedPredictions != null)
            predictionsCache.addPredictionsToSync(cachedPredictions);
        else
            predictionsCache.addPredictionsToSync(new Predictions(uid));
    }

    public static void setPredictionsToSync(Collection<String> uids) {
        predictionsCache.clearPredictionsToSync();
        for (String uid : uids)
            addPredictionsToSync(uid);
    }

    public static void registerDetailsSyncListener(AsyncCallback<User[]> listener) {
        detailsCache.registerSyncListener(listener);
    }

    public static void registerPredictionsSyncListener(AsyncCallback<Predictions[]> listener) {
        predictionsCache.registerSyncListener(listener);
    }

    public static void unregisterDetailsSyncListener(AsyncCallback<User[]> listener) {
        detailsCache.unregisterSyncListener(listener);
    }

    public static void unregisterPredictionsSyncListener(AsyncCallback<Predictions[]> listener) {
        predictionsCache.unregisterSyncListener(listener);
    }
}
