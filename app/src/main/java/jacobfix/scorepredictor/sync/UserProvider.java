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
import jacobfix.scorepredictor.Friends;
import jacobfix.scorepredictor.server.UserServerInterface;
import jacobfix.scorepredictor.task.SyncFriendsTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.UserDetails;

public class UserProvider {

    private static final String TAG = UserProvider.class.getSimpleName();

    private static UserDetailsCache userDetailsCache = new UserDetailsCache();

    public static boolean getUserDetails(final String userId, final AsyncCallback<UserDetails> callback) {
        return getUserDetails(Collections.singletonList(userId), new AsyncCallback<Map<String, UserDetails>>() {
            @Override
            public void onSuccess(Map<String, UserDetails> result) {
                callback.onSuccess(result.get(userId));
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public static boolean getUserDetails(Collection<String> userIds, final AsyncCallback<Map<String, UserDetails>> callback) {
        return getUserDetails(userIds, callback, false);
    }

    public static boolean getUserDetails(Collection<String> userIds, final AsyncCallback<Map<String, UserDetails>> callback, boolean foreground) {
        final Map<String, UserDetails> retrieved = new HashMap<>();
        LinkedList<UserDetails> needed = new LinkedList<>();

        for (String userId : userIds) {
            UserDetails cachedUserDetails = userDetailsCache.get(userId);
            if (cachedUserDetails != null) {
                retrieved.put(userId, cachedUserDetails);
            } else {
                needed.add(new UserDetails(userId));
            }
        }

        if (needed.isEmpty()) {
            callback.onSuccess(retrieved);
            return true;
        }

        userDetailsCache.sync(needed, new AsyncCallback<ArrayList<UserDetails>>() {
            @Override
            public void onSuccess(ArrayList<UserDetails> result) {
                for (UserDetails userDetails : result)
                    retrieved.put(userDetails.getId(), userDetails);
                callback.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        }, foreground);
        return false;
    }

    public static void getFriends(String userId, final AsyncCallback<Friends> callback) {
        new SyncFriendsTask(userId, new TaskFinishedListener<SyncFriendsTask>() {
            @Override
            public void onTaskFinished(SyncFriendsTask task) {
                if (task.errorOccurred()) {
                    Log.e(TAG, task.getError().toString());
                    callback.onFailure(task.getError());
                    return;
                }

                Friends friends = task.getResult();
                callback.onSuccess(friends);
            }
        }).start();
    }

    public static void getInvitees(String gameId, AsyncCallback<ArrayList<String>> callback) {

    }

    public static void registerSyncCallback(AsyncCallback<ArrayList<UserDetails>> callback) {
        userDetailsCache.registerSyncListener(callback);
    }

    public static void unregisterSyncCallback(AsyncCallback<ArrayList<UserDetails>> callback) {
        userDetailsCache.unregisterSyncListener(callback);
    }

    public static void setUserIdsToSync(Collection<String> userIds) {
        ArrayList<UserDetails> uds = new ArrayList<>();
        for (String userId : userIds) {
            UserDetails cachedUserDetails = userDetailsCache.get(userId);
            if (cachedUserDetails == null)
                cachedUserDetails = new UserDetails(userId);
            uds.add(cachedUserDetails);
        }
        userDetailsCache.setUserDetailsToSync(uds);
    }
}
