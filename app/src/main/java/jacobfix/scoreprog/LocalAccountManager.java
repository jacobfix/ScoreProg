package jacobfix.scoreprog;

import android.util.Log;

import java.util.LinkedList;

import jacobfix.scoreprog.sync.ScheduledSyncHandler;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.GetFriendsTask;
import jacobfix.scoreprog.task.SyncFriendsTask;
import jacobfix.scoreprog.task.SyncParticipantsTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.users.UserDetails;

public class LocalAccountManager {

    private static final String TAG = LocalAccountManager.class.getSimpleName();

    private static LocalAccountManager instance;

    public String userId;
    public String token;

    public Friends friends = new Friends();

    private SyncFriendsTask syncFriendsTask = new SyncFriendsTask(friends, new TaskFinishedListener<SyncFriendsTask>() {
        @Override
        public void onTaskFinished(SyncFriendsTask task) {
            if (task.errorOccurred()) {
                friendsSyncHandler.notifyAllOfFailure(task.getError());
                return;
            }
            friendsSyncHandler.notifyAllOfSuccess(null);
        }
    });
    private ScheduledSyncHandler friendsSyncHandler = new ScheduledSyncHandler(syncFriendsTask, 0, 60);

    public static synchronized LocalAccountManager get() {
        if (instance == null)
            instance = new LocalAccountManager();
        return instance;
    }

    public void setUser(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public void registerFriendsSyncListener(AsyncCallback callback) {
        friendsSyncHandler.registerSyncListener(callback);
    }

    public void unregisterFriendsSyncListener(AsyncCallback callback) {
        friendsSyncHandler.unregisterSyncListener(callback);
    }

    public Friends friends() {
        return friends;
    }

    public void clearUserInfo() {
        userId = null;
        token = null;
        friends.clear();
    }
}
