package jacobfix.scorepredictor.task;

import java.util.Collection;

import jacobfix.scorepredictor.FriendPredictionComparator;
import jacobfix.scorepredictor.users.User;

public class RankUsersTask extends SortTask<User> {

    private static final String TAG = RankUsersTask.class.getSimpleName();

    public RankUsersTask(Collection<User> usersToRank, String gameId, TaskFinishedListener listener) {
        super(usersToRank, new FriendPredictionComparator(gameId), listener);
    }
}
