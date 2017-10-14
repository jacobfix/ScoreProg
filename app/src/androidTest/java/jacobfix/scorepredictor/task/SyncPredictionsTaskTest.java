package jacobfix.scorepredictor.task;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.LinkedList;

import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.server.PredictionServerInterface;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SyncPredictionsTaskTest {

    private static final String TAG = SyncPredictionsTaskTest.class.getSimpleName();

    // @Test
    public void testOne() {
        PredictionServerInterface.setDefault(PredictionServerInterface.test);

        Log.wtf(TAG, "Running testOne");
        final LinkedList<Prediction> predictions = new LinkedList<>();
        predictions.add(new Prediction("1", "2"));
        predictions.add(new Prediction("3", "4"));
        predictions.add(new Prediction("5", "6"));

        for (Prediction prediction : predictions) {
            assertTrue(prediction.getAwayScore() == Prediction.NULL);
            assertTrue(prediction.getHomeScore() == Prediction.NULL);
        }

        new SyncPredictionsTask(predictions, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                Log.wtf(TAG, "testOne sync returned");
                if (task.errorOccurred()) {
                    Log.e(TAG, task.getError().toString());
                    task.getError().printStackTrace();
                    assertTrue(false);
                    return;
                }

                for (Prediction prediction : predictions) {
                    assertTrue(prediction.getAwayScore() != Prediction.NULL);
                    assertTrue(prediction.getHomeScore() != Prediction.NULL);
                    Log.d(TAG, "Away: " + prediction.getAwayScore() + ", Home: " + prediction.getHomeScore());
                }
            }
        }).startOnThisThread();
    }

    @Test
    public void testTwo() {
        PredictionServerInterface.setDefault(PredictionServerInterface.traditional);

        final String gameId = "2017081056";
        final LinkedList<Prediction> predictions = new LinkedList<>();
        predictions.add(new Prediction("1", gameId));
        predictions.add(new Prediction("NOTAREALUID", gameId));
        predictions.add(new Prediction("1", "NOTAREALGID"));

        new SyncPredictionsTask(predictions, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    assertTrue(false);
                    return;
                }

                ArrayList<Prediction> result = (ArrayList<Prediction>) task.getResult();
                assertEquals(result.size(), 1);
                assertEquals(result.get(0).getUserId(), "1");
                assertEquals(result.get(0).getGameId(), gameId);
                assertEquals(result.get(0).getAwayScore(), 3);
                assertEquals(result.get(0).getHomeScore(), 7);
            }
        }).startOnThisThread();
    }

    @Test
    public void testThree() {

    }
}