package jacobfix.scoreprog.task;

import org.json.JSONObject;

import jacobfix.scoreprog.server.PredictionServerInterface;


public class SubmitPredictionTask extends BaseTask<Boolean> {

    private String userId;
    private String gameId;
    private int awayScore;
    private int homeScore;

    public SubmitPredictionTask(String gameId, int awayScore, int homeScore, TaskFinishedListener listener) {
        super(listener);
        this.gameId = gameId;
        this.awayScore = awayScore;
        this.homeScore = homeScore;
    }

    @Override
    public void execute() {
        try {
            Thread.sleep(5000);
            JSONObject result = PredictionServerInterface.getDefault().submitPrediction(gameId, awayScore, homeScore);
            if (!result.getBoolean("success")) {
                throw new RuntimeException();
            }
            setResult(true);
        } catch (Exception e) {
            reportError(e);
            setResult(false);
        }
    }
}
