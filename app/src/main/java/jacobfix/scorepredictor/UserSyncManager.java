package jacobfix.scorepredictor;

import android.util.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.server.LocalUserJsonRetriever;
import jacobfix.scorepredictor.server.UserJsonRetriever;
import jacobfix.scorepredictor.sync.SyncListener;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SyncUsersTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;

/* All requests for Users should go through this object. */
public class UserSyncManager extends SyncManager {

    private static final String TAG = UserSyncManager.class.getSimpleName();

    private static UserSyncManager instance;

    private String mMe;

    private HashMap<String, User> mUserCache;
    // private LinkedList<String> mUsersToSync;
    private HashSet<User> mUsersToSync;
    private HashSet<String> mGamePredictionsToSync;

    // private UserJsonRetriever mJsonRetriever = new RemoteUserJsonRetriever();
    private UserJsonRetriever mJsonRetriever = new LocalUserJsonRetriever();
    // private UserJsonParser mJsonParser = new UserJsonParser();

    public static long SYNC_INITIAL_DELAY = 0;
    public static long SYNC_PERIOD = 60;

    public static UserSyncManager getInstance() {
        if (instance == null)
            instance = new UserSyncManager();
        return instance;
    }

    private UserSyncManager() {
        mUserCache = new HashMap<>();
        mUsersToSync = new HashSet<>();
        mGamePredictionsToSync = new HashSet<>();

        mScheduledTask = new SyncUsersTask(mUsersToSync, mGamePredictionsToSync, mJsonRetriever, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    notifyAllOfError();
                    return;
                }
                Log.d(TAG, "About to add new users");
                addNewUsers(mUsersToSync);
                notifyAllOfFinish();
            }
        });

        mSyncInitialDelay = SYNC_INITIAL_DELAY;
        mSyncPeriod = SYNC_PERIOD;
    }

    public Collection<User> getUsersById(Collection<String> ids, final RetrievedListener<HashMap<String, User>> listener) {
        HashMap<String, User> retrieved = new HashMap<>();
        LinkedList<String> needed = new LinkedList<>();

        for (String id : ids) {
            User cachedUser = mUserCache.get(id);
            if (cachedUser != null)
                retrieved.put(id, mUserCache.get(id));
            else
                needed.add(id);
        }

        if (needed.isEmpty())
            listener.onSuccess(retrieved);
        else
            new GetUsersTask(needed, new TaskFinishedListener() {
                @Override
                public void onTaskFinished(BaseTask task) {

                }
            }).start();
    }

    /* Retrieves a User object from the map, or null if the user is not present.
       This should be called after the user was included in a sync. */
    public User get(String userId) {
        return mUsers.get(userId);
    }

    /* This method is invoked by an activity for on-demand syncing. If an activity
       wishes to execute the same procedure it performs at the end of each scheduled
       sync, it should pass in the same SyncListener. */
    public void sync(String[] userIds, String[] gameIds, final SyncListener listener, boolean inForeground) {
        /*
        final HashSet<User> usersToSync = new HashSet<>();
        for (String userId : userIds)
            usersToSync.add(getUserCreateIfNew(userId));
            */
        final User[] usersToSync = new User[userIds.length];
        for (int i = 0; i < userIds.length; i++) {
            Log.d(TAG, "User: " + userIds[i]);
            usersToSync[i] = getUserCreateIfNew(userIds[i]);
        }

        Log.d(TAG, "About to create SyncUsersTask");
        if (gameIds == null)
            gameIds = new String[0];
        SyncUsersTask t = new SyncUsersTask(Arrays.asList(usersToSync), Arrays.asList(gameIds), mJsonRetriever, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    if (listener != null)
                        listener.onSyncError();
                    return;
                }
                addNewUsers(usersToSync);
                if (listener != null)
                    listener.onSyncFinished();
            }
        });

        Log.d(TAG, "About to start execution");
        if (inForeground)
            t.startOnThisThread();
        else
            t.start();
    }

    public void syncInForeground(String[] userIds, String[] gameIds, SyncListener listener) {
        sync(userIds, gameIds, listener, true);
    }

    public void syncInForeground(String userId, String[] gameIds, SyncListener listener) {
        Log.d(TAG, "syncInForeground()");
        syncInForeground(new String[]{userId}, gameIds, listener);
    }

    public void setMe(String userId) {
        mMe = userId;
    }

    public User me() {
        return mUsers.get(mMe);
    }

    public void addUserToBackgroundSync(String userId) {
        mUsersToSync.add(getUserCreateIfNew(userId));
        Log.d(TAG, "mUsersToSync after addition:");
        Log.d(TAG, mUsersToSync.toString());
    }

    public void setUsersOfBackgroundSync(Collection<String> userIds) {
        mUsersToSync.clear();
        for (String u : userIds)
            mUsersToSync.add(getUserCreateIfNew(u));
    }

    public void addPredictionToBackgroundSync(String gameId) {
        mGamePredictionsToSync.add(gameId);
    }

    public void setPredictionsOfBackgroundSync(Collection<String> gameIds) {
        mGamePredictionsToSync.clear();
        mGamePredictionsToSync.addAll(gameIds);
        Log.d(TAG, "setPredictionsOfBackgroundSync");
        Log.d(TAG, mGamePredictionsToSync.toString());
    }

    public Collection<User> getParticipatingUsers(String gameId, Collection<String> userIds) {
        Log.d(TAG, "In getParticipatingUsers()");
        Log.d(TAG, userIds.toString());
        Collection<User> participants = new HashSet<>();
        for (String userId : userIds) {
            User user = mUsers.get(userId);
            Log.d(TAG, "Got user (ID " + userId + "), " + user);
            if (user != null && user.isPlaying(gameId))
                participants.add(mUsers.get(userId));
        }
        return participants;
    }

    public Collection<User> getParticipatingFriends(String gameId) {
        return getParticipatingUsers(gameId, me().getFriends());
    }

    private User getUserCreateIfNew(String userId) {
        User user = mUsers.get(userId);
        if (user == null)
            user = new User(userId);
        return user;
    }

    private void addNewUsers(Collection<User> toAdd) {
        Log.d(TAG, "TO ADD: " + toAdd.toString());
        for (User user : toAdd)
            if (!mUsers.containsKey(user.getId()))
                mUsers.put(user.getId(), user);
    }

    private void addNewUsers(User[] toAdd) {
        for (User user : toAdd) {
            if (!mUsers.containsKey(user.getId()))
                mUsers.put(user.getId(), user);
        }
    }
}
