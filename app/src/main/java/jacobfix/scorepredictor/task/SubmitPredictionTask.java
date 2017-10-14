package jacobfix.scorepredictor.task;

import org.json.JSONObject;

import jacobfix.scorepredictor.server.PredictionServerInterface;

public class SubmitPredictionTask extends BaseTask<Boolean> {

    private String userId;
    private String gameId;
    private int awayScore;
    private int homeScore;

    public SubmitPredictionTask(String userId, String gameId, int awayScore, int homeScore, TaskFinishedListener listener) {
        super(listener);
        this.userId = userId;
        this.gameId = gameId;
        this.awayScore = awayScore;
        this.homeScore = homeScore;
    }

    @Override
    public void execute() {
        try {
            JSONObject result = PredictionServerInterface.getDefault().putPrediction(userId, gameId, awayScore, homeScore);
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
