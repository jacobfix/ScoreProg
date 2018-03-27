package jacobfix.scoreprog;

public abstract class PredictableScorer {

    protected int mPredictedScore;

    public static int NO_PREDICTION = -1;

    public PredictableScorer() {
        mPredictedScore = NO_PREDICTION;
    }

    public int getPredictedScore() {
        return mPredictedScore;
    }

    public void setPredictedScore(int prediction) {
        mPredictedScore = prediction;
    }
    
    public boolean isPredicted() {
        return mPredictedScore != NO_PREDICTION;
    }
}
