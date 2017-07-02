package jacobfix.scorepredictor;

import java.util.Comparator;

import jacobfix.scorepredictor.users.User;

public class FriendPredictionComparator implements Comparator<User> {

    private NflGame mGame;

    public FriendPredictionComparator(NflGame game) {
        mGame = game;
    }

    public int compare(User f1, User f2) {
        return f1.getPrediction(mGame.getGameId()).getSpread(mGame) - f2.getPrediction(mGame.getGameId()).getSpread(mGame);
    }
}
