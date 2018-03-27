package jacobfix.scoreprog.sync;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.LocalAccountManager;
import jacobfix.scoreprog.Participants;
import jacobfix.scoreprog.Prediction;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PredictionProviderTest {

    private static final String TAG = PredictionProviderTest.class.getSimpleName();

    @Test
    public void testGetMyPrediction() throws Exception {
        LocalAccountManager.get().setUser("f3aaa55932d784a43edb97435249d1354ada07a3", "ca2ab1c2360e84ca");

        Log.d(TAG, "Running testGetMyPrediction");
        PredictionProvider.getMyPrediction("10", new AsyncCallback<Prediction>() {
            @Override
            public void onSuccess(Prediction result) {
                Log.d(TAG, "testGetMyPrediction success");
                assertNotNull(result);
                assertEquals("ca2ab1c2360e84ca", result.getUserId());
                assertEquals("10", result.getGameId());
                assertEquals(14, result.getAwayScore());
                assertEquals(21, result.getHomeScore());
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "testGetMyPrediction failure");
                Log.e(TAG, e.toString());
                assertNull(e);
            }
        });
    }

    @Test
    public void testGetMyPredictions() throws Exception {
        LocalAccountManager.get().setUser("f3aaa55932d784a43edb97435249d1354ada07a3", "ca2ab1c2360e84ca");

        Log.d(TAG, "Running testGetMyPredictions");
        List<String> gameIds = new ArrayList<>();
        gameIds.add("10");
        gameIds.add("11");

        PredictionProvider.getMyPredictions(gameIds, new AsyncCallback<Map<String, Prediction>>() {
            @Override
            public void onSuccess(Map<String, Prediction> result) {
                Log.d(TAG, "testGetMyPredictions success");
                assertNotNull(result);

                Prediction p1 = result.get("10");
                Prediction p2 = result.get("11");

                assertNotNull(p1);
                assertNotNull(p2);

                assertEquals("ca2ab1c2360e84ca", p1.getUserId());
                assertEquals("ca2ab1c2360e84ca", p2.getUserId());
                assertEquals("10", p1.getGameId());
                assertEquals("11", p2.getGameId());

                assertEquals(14, p1.getAwayScore());
                assertEquals(21, p1.getHomeScore());
                assertEquals(7, p2.getAwayScore());
                assertEquals(42, p2.getHomeScore());
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "testGetMyPredictions failure");
                assertNull(e);
            }
        });
    }

    @Test
    public void testGetParticipants() throws Exception {
        LocalAccountManager.get().setUser("f3aaa55932d784a43edb97435249d1354ada07a3", "ca2ab1c2360e84ca");

        Log.d(TAG, "Running testGetParticipants");
        PredictionProvider.getParticipants("10", new AsyncCallback<Participants>() {
            @Override
            public void onSuccess(Participants result) {
                Log.d(TAG, "testGetParticipants success");
                assertNotNull(result);
                assertEquals("10", result.getGameId());

                Map<String, Prediction> predictions = result.getPredictions();
                assertEquals(1, predictions.size());

                Prediction participantPrediction = predictions.get("11184c86f197273b");
                assertNotNull(participantPrediction);

                assertEquals("10", participantPrediction.getGameId());
                assertEquals(21, participantPrediction.getAwayScore());
                assertEquals(35, participantPrediction.getHomeScore());
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "testGetParticipants failure");
                Log.d(TAG, "Exception: " + e.toString());
                assertNull(e);
            }
        });
    }
}