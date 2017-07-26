package jacobfix.scorepredictor;

import android.util.Log;

public class Prediction {

    private static final String TAG = Prediction.class.getSimpleName();

    private String gameId;

    private int mAwayScore = NULL;
    private int mHomeScore = NULL;
    
    private static final int NULL = -1;
    
    public static final int W_AWAY = 0;
    public static final int W_HOME = 1;
    public static final int W_TIE = 2;
    public static final int W_NONE = 3;

    public Prediction(String gid) {
        gameId = gid;
        mAwayScore = NULL;
        mHomeScore = NULL;
    }

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
    
    public boolean isComplete() {
        return mAwayScore != NULL && mHomeScore != NULL;
    }
    
    public int winner() {
        if (!isComplete())
            return W_NONE;
        
        int t = mAwayScore - mHomeScore;
        if (t > 0)
            return W_AWAY;
        if (t < 0)
            return W_HOME;
        return W_TIE;
    }
}
