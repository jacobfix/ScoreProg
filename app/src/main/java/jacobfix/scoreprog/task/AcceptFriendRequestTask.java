package jacobfix.scoreprog.task;

import jacobfix.scoreprog.server.UserServerInterface;

public class AcceptFriendRequestTask extends BaseTask {

    private String userId;

    public AcceptFriendRequestTask(String userId, TaskFinishedListener listener) {
        super(listener);
        this.userId = userId;
    }

    @Override
    public void execute() {
        try {
            UserServerInterface.getDefault().confirmFriendRequest(userId);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
