package jacobfix.scoreprog;

public class PredictionDetails {

    public String gameId;
    public boolean complete;
    public int awayScore;
    public int homeScore;
    public int winner;

    public static PredictionDetails capture(Prediction prediction) {
        PredictionDetails predictionDetails = new PredictionDetails();
        synchronized (prediction.acquireLock()) {
            predictionDetails.gameId = prediction.getGameId();
            predictionDetails.complete = prediction.isComplete();
            predictionDetails.awayScore = prediction.getAwayScore();
            predictionDetails.homeScore = prediction.getHomeScore();
            predictionDetails.winner = prediction.winner();
        }
        return predictionDetails;
    }
}
