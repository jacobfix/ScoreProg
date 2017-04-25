package jacobfix.scorepredictor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

import jacobfix.scorepredictor.friends.User;

public class SortFriendsTask extends BaseTask<ArrayList<User>> {

    private LinkedList<User> mFriendsList;
    private String mGameId;

    public SortFriendsTask(LinkedList<User> friends, String gameId, TaskFinishedListener listener) {
        super(listener);
        mFriendsList = friends;
        mGameId = gameId;
    }

    public void execute() {
        ArrayList<User> sortedFriends = new ArrayList<User>();
        FriendPredictionComparator comparator = new FriendPredictionComparator(mGameId);
        for (User f : mFriendsList) {
            if (f.getPrediction(mGameId) != null) {
                insertOrdered(sortedFriends, f, comparator);
            }
        }
        mResult = sortedFriends;
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
