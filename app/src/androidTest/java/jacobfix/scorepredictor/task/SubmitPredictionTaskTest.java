package jacobfix.scorepredictor.task;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.server.PredictionServerInterface;
import jacobfix.scorepredictor.sync.PredictionProvider;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SubmitPredictionTaskTest {

    @Test
    public void testOne() {
        PredictionServerInterface.setDefault(PredictionServerInterface.traditional);

        new SubmitPredictionTask("1", "2017081352", 14, 17, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    assertTrue(false);
                    return;
                }
                assertTrue((boolean) task.getResult());
            }
        }).startOnThisThread();
    }

    @Test
    public void testTwo() {
        PredictionServerInterface.setDefault(PredictionServerInterface.traditional);

        final String userId = "1";
        final String gameId = "2017081055";
        final int awayScore = 30;
        final int homeScore = 27;

        new SubmitPredictionTask(userId, gameId, awayScore, homeScore, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    assertTrue(false);
                    return;
                }
                assertTrue((boolean) task.getResult());
            }
        }).startOnThisThread();

        PredictionProvider.getPrediction(userId, gameId, new AsyncCallback<Prediction>() {
            @Override
            public void onSuccess(Prediction result) {
                assertEquals(result.getUserId(), userId);
                assertEquals(result.getGameId(), gameId);
                assertEquals(result.getAwayScore(), awayScore);
                assertEquals(result.getHomeScore(), homeScore);
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue(false);
            }
        });
    }
}