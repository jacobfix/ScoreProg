package jacobfix.scorepredictor;

import java.util.Comparator;

import jacobfix.scorepredictor.users.User;

public class FriendPredictionComparator implements Comparator<User> {

    private String mGameId;

    public FriendPredictionComparator(String gameId) {
        mGameId = gameId;
    }

    public int compare(User f1, User f2) {
        return f1.getPrediction(mGameId).compareTo(f2.getPrediction(mGameId));
    }
}
