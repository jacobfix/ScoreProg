package jacobfix.scorepredictor;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import jacobfix.scorepredictor.friends.User;

public class RankUsersTask extends BaseTask {

    private static final String TAG = RankUsersTask.class.getSimpleName();

    private Collection<User> mUsers;
    private String mGameId;

    public RankUsersTask(Collection<User> usersToRank, String gameId, TaskFinishedListener listener) {
        super(listener);
        mUsers = usersToRank;
        mGameId = gameId;
    }

    @Override
    public void execute() {
        ArrayList<User> rankedUsers = new ArrayList<>();
        FriendPredictionComparator comparator = new FriendPredictionComparator(mGameId);
        for (User user : mUsers) {
            if (user.getPrediction(mGameId) != null) {
                insertOrdered(rankedUsers, user, comparator);
            } else {
                Log.d(TAG, "A nonparticipating user was passed to RankUsersTask");
            }
        }
        mResult = rankedUsers;
    }

    private void insertOrdered(ArrayList<User> sortedList, User friend, Comparator comparator) {
        for (int i = 0; i < sortedList.size(); i++) {
            if (comparator.compare(friend, sortedList.get(i)) <= 0) {
                sortedList.add(i, friend);
                return;
            }
        }
        sortedList.add(friend);
    }

}
