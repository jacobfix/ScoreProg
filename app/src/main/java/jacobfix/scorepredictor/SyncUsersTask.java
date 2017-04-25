package jacobfix.scorepredictor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jacobfix.scorepredictor.friends.User;

public class SyncUsersTask extends BaseTask<LinkedList<User>> {

    private User[] mUsersToSync;

    public SyncUsersTask(User[] users, TaskFinishedListener listener) {
        super(listener);
        mUsersToSync = users;
    }

    public SyncUsersTask(Collection<User> users, TaskFinishedListener listener) {
        this(users.toArray(new User[0]), listener);
    }

    @Override
    public void execute() {

    }
}
