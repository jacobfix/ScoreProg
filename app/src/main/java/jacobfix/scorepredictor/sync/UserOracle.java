package jacobfix.scorepredictor.sync;

import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jacobfix.scorepredictor.server.LocalUserJsonRetriever;
import jacobfix.scorepredictor.server.UserJsonRetriever;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.deprecated.OriginalSyncUsersTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;

public class UserOracle extends BaseOracle {

    private static final String TAG = UserOracle.class.getSimpleName();

    private static UserOracle instance;

    private User mMe;
    public HashMap<String, User> mUsers;
    private HashSet<User> mUsersToSync;
    private HashSet<String> mPredictionsToSync;

    private TaskFinishedListener mPostSyncProcedure;
    private UserJsonRetriever mJsonRetriever;

    public static long SYNC_INITIAL_DELAY = 0;
    public static long SYNC_PERIOD = 60;

    public static synchronized UserOracle getInstance() {
        if (instance == null) {
            instance = new UserOracle();
        }
        return instance;
    }

    public UserOracle() {
        mUsers = new HashMap<String, User>();
    }

    @Override
    public OriginalSyncUsersTask initScheduledSyncTask() {
        mUsersToSync = new HashSet<User>();
        mPostSyncProcedure = new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    notifyOfSyncError(task.getError());
                    return;
                }
                addAnyNewUsers(mUsersToSync);
                notifyOfSyncFinished();
            }
        };
        // mJsonRetriever = new RemoteUserJsonRetriever();
        mJsonRetriever = new LocalUserJsonRetriever();
        mPredictionsToSync = new HashSet<>();
        return new OriginalSyncUsersTask(mUsersToSync, mPredictionsToSync, mJsonRetriever, mPostSyncProcedure);
    }

    @Override
    public long getSyncInitialDelay() {
        return SYNC_INITIAL_DELAY;
    }

    @Override
    public long getSyncPeriod() {
        return SYNC_PERIOD;
    }

    public void setMe(String userId) {
        mMe = mUsers.get(userId);
    }

    public User me() {
        return mMe;
    }

    public boolean isMe(User user) {
        return mMe == user;
    }

    public void sync(String userId) {
        /* Do in a background thread by default. */
        sync(userId, false);
    }

    public void sync(String userId, boolean inForeground) {
        syncAll(Collections.singletonList(userId), inForeground);
    }

    public void syncAll(List<String> userIds) {
        /* Do in background by default. */
        syncAll(userIds, false);
    }

    public void syncAll(List<String> userIds, boolean inForeground) {
        // clearSyncError();
        final HashSet<User> usersToSync = new HashSet<>();
        for (String userId : userIds)
            usersToSync.add(getUserCreateIfNew(userId));

        OriginalSyncUsersTask syncTask = new OriginalSyncUsersTask(usersToSync, mJsonRetriever, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                Log.d(TAG, "In the finished listener of the sync task");
                if (task.errorOccurred()) {
                    notifyOfSyncError(task.getError());
                    return;
                }
                Log.d(TAG, "About to add new users!");
                addAnyNewUsers(usersToSync);
                notifyOfSyncFinished();
            }
        });

        if (inForeground)
            syncTask.startOnThisThread();
        else
            syncTask.start();
    }

    private void addAnyNewUsers(Collection<User> updated) {
        for (User user : updated) {
            if (!mUsers.containsKey(user.getId())) {
                mUsers.put(user.getId(), user);
            }
        }
    }

    public Collection<User> getParticipatingFriends(String gameId) {
        // TODO: Problem is that we are currently not setting a "me" when the app launches
        Log.d(TAG, "getParticipatingFriends() " + mMe.getFriends());
        return getParticipatingUsers(gameId, mMe.getFriends());
    }

    public Collection<User> getParticipatingUsers(String gameId, Collection<String> userIds) {
        Collection<User> participants = new HashSet<>();
        for (String userId : userIds) {
            if (mUsers.get(userId).isPlaying(gameId)) {
                participants.add(mUsers.get(userId));
            }
        }
        return participants;
    }

    public void addUserToScheduledSync(String userId) {
        if (!mUsersToSync.contains(userId))
            mUsersToSync.add(getUserCreateIfNew(userId));
        else
            Log.d(TAG, "Attempted to add an already present user to the scheduled sync");
    }

    public void clearUsersInScheduledSync() {
        mUsersToSync.clear();
    }

    public void addPredictionToScheduledSync(String gameId) {
        if (!mPredictionsToSync.contains(gameId))
            mPredictionsToSync.add(gameId);
        else
            Log.d(TAG, "Attempted to add an already present game to the prediction sync set");
    }

    public void clearPredictionsInScheduledSync() {
        mPredictionsToSync.clear();
    }

    private User getUserCreateIfNew(String userId) {
        User user = mUsers.get(userId);
        if (user == null) {
            user = new User(userId);
        }
        return user;
    }

    public boolean userExists(String userId) {
        return mUsers.containsKey(userId);
    }

    public UserJsonRetriever getUserJsonRetriever() {
        return mJsonRetriever;
    }
}
