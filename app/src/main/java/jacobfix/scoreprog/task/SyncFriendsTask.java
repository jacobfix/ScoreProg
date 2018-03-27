package jacobfix.scoreprog.task;

import org.json.JSONArray;

import jacobfix.scoreprog.Friends;
import jacobfix.scoreprog.server.JsonParser;
import jacobfix.scoreprog.server.UserServerInterface;

public class SyncFriendsTask extends BaseTask<Friends> {

    private Friends friends;

    public SyncFriendsTask(Friends friends, TaskFinishedListener listener) {
        super(listener);
        this.friends = friends;
    }

    @Override
    public void execute() {
        try {
            JSONArray friendsJson = UserServerInterface.getDefault().getFriends(UserServerInterface.FRIENDS_ALL);
            synchronized (friends.acquireLock()) {
                JsonParser.updateFriends(friends, friendsJson);
            }
        } catch (Exception e) {
            reportError(e);
        }
    }

    public enum FriendStatus {
        CONFIRMED,
        PENDING_SENT,
        PENDING_RECEIVED,
        BLOCKED
    }
}
