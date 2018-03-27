package jacobfix.scoreprog.sync;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.Friends;
import jacobfix.scoreprog.LocalAccountManager;
import jacobfix.scoreprog.task.GetFriendsTask;
import jacobfix.scoreprog.task.SyncFriendsTask;
import jacobfix.scoreprog.task.SyncGamesTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.users.UserDetails;

public class UserProvider {

    private static final String TAG = UserProvider.class.getSimpleName();

    private static UserDetailsCache userDetailsCache = new UserDetailsCache();

    public static void getFriends(final AsyncCallback<Friends> callback) {
        new GetFriendsTask(new TaskFinishedListener<GetFriendsTask>() {
            @Override
            public void onTaskFinished(GetFriendsTask task) {

            }
        });
    }

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

    public static boolean getUserDetails(final String userId, final AsyncCallback<UserDetails> callback, boolean foreground) {
        return getUserDetails(Collections.singletonList(userId), new AsyncCallback<Map<String, UserDetails>>() {
            @Override
            public void onSuccess(Map<String, UserDetails> result) {
                callback.onSuccess(result.get(userId));
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        }, foreground);
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

        userDetailsCache.sync(needed, new AsyncCallback<Collection<UserDetails>>() {
            @Override
            public void onSuccess(Collection<UserDetails> result) {
                for (UserDetails userDetails : result)
                    retrieved.put(userDetails.getUserId(), userDetails);
                callback.onSuccess(retrieved);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        }, foreground);
        return false;
    }

    public static void clearUserDetailsCache() {
        userDetailsCache.clear();
    }

    public static void getInvitees(String gameId, AsyncCallback<ArrayList<String>> callback) {

    }

    public static void registerSyncCallback(AsyncCallback<Collection<UserDetails>> callback) {
        userDetailsCache.registerSyncListener(callback);
    }

    public static void unregisterSyncCallback(AsyncCallback<Collection<UserDetails>> callback) {
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
