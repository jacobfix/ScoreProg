package jacobfix.scorepredictor;

import android.provider.Telephony;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import jacobfix.scorepredictor.sync.PredictionProvider;
import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.RankPredictionsTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.UserDetails;
import jacobfix.scorepredictor.util.Pair;
import jacobfix.scorepredictor.util.Util;

public class Participants {

    private static final String TAG = Participants.class.getSimpleName();

    private Map<String, UserDetails> userDetails;
    private Predictions predictions;

    public void setPredictions(Predictions predictions) {
        this.predictions = predictions;
    }

    public void setUserDetails(Map<String, UserDetails> userDetails) {
        this.userDetails = userDetails;
    }

    public Prediction getPrediction(String userId) {
        return predictions.get(userId).pollFirst();
    }

    public UserDetails getUserDetails(String userId) {
        return userDetails.get(userId);
    }

    public void rank(final FullGame game, final AsyncCallback<ArrayList<Pair<Integer, String>>> callback) {
        final Collection<Prediction> unsorted = predictions.getAll();
        Prediction.disableUpdates(unsorted);

        synchronized (game.getLock()) {
            game.restrictUpdates();
        }

        new RankPredictionsTask(game, unsorted, new TaskFinishedListener<RankPredictionsTask>() {
            @Override
            public void onTaskFinished(RankPredictionsTask task) {
                if (task.errorOccurred()) {
                    Prediction.enableUpdates(unsorted);

                    synchronized (game.getLock()) {
                        game.allowUpdates();
                    }

                    Log.e(TAG, task.getError().toString());
                    callback.onFailure(task.getError());
                    return;
                }

                ArrayList<Prediction> sortedPredictions = task.getResult();
                ArrayList<Pair<Integer, String>> ranking = new ArrayList<>();

                if (sortedPredictions.size() < 1)
                    callback.onSuccess(ranking);

                int currentRank = 1;
                ranking.add(new Pair<>(currentRank, sortedPredictions.get(0).getUserId()));
                for (int i = 1; i < sortedPredictions.size(); i++) {
                    if (sortedPredictions.get(i).getSpread(game) != sortedPredictions.get(i - 1).getSpread(game))
                        currentRank++;
                    ranking.add(new Pair<>(currentRank, sortedPredictions.get(i).getUserId()));
                }

                Prediction.enableUpdates(unsorted);

                synchronized (game.getLock()) {
                    game.allowUpdates();
                }

                callback.onSuccess(ranking);
            }
        }).start();
    }
}
