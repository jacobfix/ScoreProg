package jacobfix.scorepredictor.task;

import org.json.JSONArray;
import org.json.JSONObject;

import jacobfix.scorepredictor.Friends;
import jacobfix.scorepredictor.server.UserServerInterface;

public class SyncFriendsTask extends BaseTask<Friends> {

    private String userId;

    public static final int F_CONFIRMED = 1;
    public static final int F_PENDING_SENT = 2;
    public static final int F_PENDING_RECEIVED = 3;
    public static final int F_BLOCKED = 4;

    public SyncFriendsTask(String userId, TaskFinishedListener listener) {
        super(listener);
        this.userId = userId;
    }

    @Override
    public void execute() {
        try {
            JSONObject json = UserServerInterface.getDefault().getFriendsJson(userId);
            if (!json.getBoolean("success"))
                throw new Exception(json.getString("errno"));

            Friends friends = new Friends();
            JSONArray friendsJson = json.getJSONArray("friends");
            for (int i = 0; i < friendsJson.length(); i++) {
                JSONObject friendJson = friendsJson.getJSONObject(i);
                String friendId = friendJson.getString("uid");
                switch (friendJson.getInt("status")) {
                    case F_CONFIRMED:
                        friends.addConfirmed(friendId);
                        break;
                    case F_PENDING_SENT:
                        friends.addPending(friendId);
                        break;
                    case F_PENDING_RECEIVED:
                        friends.addPending(friendId);
                        break;
                    case F_BLOCKED:
                        friends.addBlocked(friendId);
                        break;
                }
            }
            setResult(friends);
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
