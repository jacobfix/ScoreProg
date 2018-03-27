package jacobfix.scoreprog.task;

import jacobfix.scoreprog.LocalAccountManager;
import jacobfix.scoreprog.server.UserServerInterface;

public class SendFriendRequestTask extends BaseTask<Boolean> {

    private String userId;

    public SendFriendRequestTask(String userId, TaskFinishedListener listener) {
        super(listener);
        this.userId = userId;
    }

    @Override
    public void execute() {
        try {
            if (userId.equals(LocalAccountManager.get().userId))
                throw new RuntimeException("User cannot request to be friends with himself");

            boolean success = UserServerInterface.getDefault().requestFriend(userId);
            setResult(success);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
