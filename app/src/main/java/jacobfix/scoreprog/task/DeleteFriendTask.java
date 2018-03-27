package jacobfix.scoreprog.task;

import jacobfix.scoreprog.server.UserServerInterface;

public class DeleteFriendTask extends BaseTask {

    private String userId;

    public DeleteFriendTask(String userId, TaskFinishedListener listener) {
        super(listener);
        this.userId = userId;
    }

    @Override
    public void execute() {
        try {
            UserServerInterface.getDefault().cancelFriendRequest(userId);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
