package jacobfix.scoreprog.task;

import java.util.Collection;

import jacobfix.scoreprog.FullGame;
import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.Prediction;
import jacobfix.scoreprog.PredictionComparator;

public class RankPredictionsTask extends SortTask<Prediction> {

    private static final String TAG = RankPredictionsTask.class.getSimpleName();

    public RankPredictionsTask(Game game, Collection<Prediction> toRank, TaskFinishedListener listener) {
        super(toRank, new PredictionComparator(game), listener);
    }
}
