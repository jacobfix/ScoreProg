package jacobfix.scorepredictor;

import android.util.Log;

public class Prediction implements Comparable<Prediction> {

    private static final String TAG = Prediction.class.getSimpleName();

    private NflGame mGame;

    private int mAwayScore;
    private int mHomeScore;

    private int mSpread;

    public Prediction(int awayScore, int homeScore) {
        mAwayScore = awayScore;
        mHomeScore = homeScore;
        // updateSpread();
    }

    public void setAwayScore(int score) {
        mAwayScore = score;
        // updateSpread();
    }

    public int getAwayScore() {
        return mAwayScore;
    }

    public void setHomeScore(int score) {
        mHomeScore = score;
        // updateSpread();
    }

    public int getHomeScore() {
        return mHomeScore;
    }

    private void updateSpread(NflGame game) {
        mSpread = Math.abs(mAwayScore - game.getAwayTeam().getScore()) + Math.abs(mHomeScore - game.getHomeTeam().getScore());
    }

    private int getSpread() {
        return mSpread;
    }

    public int compareTo(Prediction other) {
        /* Defines the relation between two Predictions as:
           {p1, p2}, such that p1.mSpread <= p2.mSpread */
        if (other == null) {
            throw new NullPointerException("Tried to compare prediction to null");
        }
        return mSpread - other.mSpread;
    }
}
