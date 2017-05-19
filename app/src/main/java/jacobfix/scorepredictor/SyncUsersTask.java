package jacobfix.scorepredictor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jacobfix.scorepredictor.friends.User;

public class SyncUsersTask extends BaseTask<LinkedList<User>> {

    private Collection<User> mUsersToSync;

    public SyncUsersTask(Collection<User> users, TaskFinishedListener listener) {
        super(listener);
        mUsersToSync = users;
    }

    @Override
    public void execute() {

    }
}
