package jacobfix.scorepredictor.task;

import jacobfix.scorepredictor.users.User;

public class GetFriendsTask extends BaseTask {

    private User mUser;

    public GetFriendsTask(User user) {
        mUser = user;
    }

    @Override
    public void execute() {

    }
}
