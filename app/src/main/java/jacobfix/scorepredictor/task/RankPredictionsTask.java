package jacobfix.scorepredictor.task;

import java.util.Collection;

import jacobfix.scorepredictor.FullGame;
import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.PredictionComparator;

public class RankPredictionsTask extends SortTask<Prediction> {

    private static final String TAG = RankPredictionsTask.class.getSimpleName();

    public RankPredictionsTask(FullGame game, Collection<Prediction> toRank, TaskFinishedListener listener) {
        super(toRank, new PredictionComparator(game), listener);
    }
}
