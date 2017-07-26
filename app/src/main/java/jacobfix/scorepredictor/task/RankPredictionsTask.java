package jacobfix.scorepredictor.task;

import java.util.Collection;

import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.Predictions;
import jacobfix.scorepredictor.PredictionsComparator;

public class RankPredictionsTask extends SortTask<Predictions> {

    private static final String TAG = RankPredictionsTask.class.getSimpleName();

    public RankPredictionsTask(NflGame game, Collection<Predictions> toRank, TaskFinishedListener listener) {
        super(toRank, new PredictionsComparator(game), listener);
    }
}
