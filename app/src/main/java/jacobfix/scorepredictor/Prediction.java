package jacobfix.scorepredictor;

import android.util.Log;

public class Prediction {

    private static final String TAG = Prediction.class.getSimpleName();

    private String gameId;

    private int mAwayScore;
    private int mHomeScore;

    public Prediction(String gid, int awayScore, int homeScore) {
        gameId = gid;
        mAwayScore = awayScore;
        mHomeScore = homeScore;
    }

    public void setAwayScore(int score) {
        mAwayScore = score;
    }

    public int getAwayScore() {
        return mAwayScore;
    }

    public void setHomeScore(int score) {
        mHomeScore = score;
    }

    public int getHomeScore() {
        return mHomeScore;
    }

    public int getSpread(NflGame game) {
        return Math.abs(mAwayScore - game.getAwayTeam().getScore()) + Math.abs(mHomeScore - game.getHomeTeam().getScore());
    }
}
