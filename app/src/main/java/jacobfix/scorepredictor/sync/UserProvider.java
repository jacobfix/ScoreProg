package jacobfix.scorepredictor.sync;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.Predictions;
import jacobfix.scorepredictor.ResultListener;
import jacobfix.scorepredictor.sync.DetailsCache;
import jacobfix.scorepredictor.sync.PredictionsCache;
import jacobfix.scorepredictor.users.User;

public class UserProvider {

    private static final DetailsCache detailsCache = new DetailsCache();
    private static final PredictionsCache predictionsCache = new PredictionsCache();

    public static boolean getUserDetails(Collection<String> ids, final ResultListener<Map<String, User>> listener) {
        final HashMap<String, User> retrieved = new HashMap<>();
        final LinkedList<User> needed = new LinkedList<>();

        for (String id : ids) {
            User cachedUser = detailsCache.get(id);
            if (cachedUser != null)
                retrieved.put(id, cachedUser);
            else
                needed.add(new User(id));
        }

        if (needed.isEmpty()) {
            listener.onSuccess(retrieved);
            return true;
        }

        detailsCache.sync(needed, new ResultListener<User[]>() {
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

    public static boolean getUserPredictions(String gid, Collection<String> uids, final ResultListener<Map<String, Predictions>> listener) {
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

        predictionsCache.sync(gid, needed, new ResultListener<Predictions[]>() {
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

    public static void registerDetailsSyncListener(ResultListener<?> listener) {
        detailsCache.registerSyncListener(listener);
    }

    public static void registerPredictionsSyncListener(ResultListener<?> listener) {
        predictionsCache.registerSyncListener(listener);
    }

    public static void unregisterDetailsSyncListener(ResultListener<?> listener) {
        detailsCache.unregisterSyncListener(listener);
    }

    public static void unregisterPredictionSyncListener(ResultListener<?> listener) {
        predictionsCache.unregisterSyncListener(listener);
    }
}
