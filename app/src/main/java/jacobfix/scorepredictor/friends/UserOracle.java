package jacobfix.scorepredictor.friends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import jacobfix.scorepredictor.BaseTask;
import jacobfix.scorepredictor.Oracle;
import jacobfix.scorepredictor.UserOracleSyncListener;
import jacobfix.scorepredictor.SyncUsersTask;
import jacobfix.scorepredictor.TaskFinishedListener;

public class UserOracle extends Oracle {

    private static UserOracle instance;

    private User mMe;
    private HashMap<String, User> mUsers;
    private HashSet<User> mUsersToSync;

    private TaskFinishedListener mPostSyncProcedure;

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
    public SyncUsersTask initScheduledSyncTask() {
        mUsersToSync = new HashSet<User>();
        mPostSyncProcedure = new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                /* Add any newly sync'd Users to the map. */
                for (User user : mUsersToSync) {
                    if (!mUsers.containsKey(user.getId())) {
                        mUsers.put(user.getId(), user);
                    }
                }
                notifyAllSyncListeners();
            }
        };
        return new SyncUsersTask(mUsersToSync, mPostSyncProcedure);
    }

    @Override
    public long getSyncInitialDelay() {
        return SYNC_INITIAL_DELAY;
    }

    @Override
    public long getSyncPeriod() {
        return SYNC_PERIOD;
    }

    public void setMe(User user) {
        mMe = user;
    }

    public User me() {
        return mMe;
    }

    public boolean isMe(User user) {
        return mMe == user;
    }

    public void sync(String userId) {
        syncAll(Arrays.asList(userId));
    }

    public void syncAll(List<String> userIds) {
        /* Translates the list of user IDs into a list of User objects, then passes this list into
           the sync task. */
        HashSet<User> usersToSync = new HashSet<User>();
        for (String userId : userIds) {
            usersToSync.add(getUserCreateIfNew(userId));
        }
        new SyncUsersTask(usersToSync, mPostSyncProcedure).start();
    }

    public Collection<User> getParticipatingFriends(String gameId) {
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
        mUsersToSync.add(getUserCreateIfNew(userId));
    }

    private User getUserCreateIfNew(String userId) {
        User user = mUsers.get(userId);
        if (user == null) {
            user = new User(userId);
        }
        return user;
    }
}
