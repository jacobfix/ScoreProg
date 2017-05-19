package jacobfix.scorepredictor;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import jacobfix.scorepredictor.friends.User;

public class RankUsersTask extends SortTask<User> {

    private static final String TAG = RankUsersTask.class.getSimpleName();

    public RankUsersTask(Collection<User> usersToRank, String gameId, TaskFinishedListener listener) {
        super(usersToRank, new FriendPredictionComparator(gameId), listener);
    }
}
