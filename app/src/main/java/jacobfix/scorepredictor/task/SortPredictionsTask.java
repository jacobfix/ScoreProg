package jacobfix.scorepredictor.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.PredictionComparator;

public class SortPredictionsTask extends SortTask<Prediction> {

    public SortPredictionsTask(Collection<Prediction> itemsToSort, Comparator comparator, TaskFinishedListener listener) {
        super(itemsToSort, comparator, listener);
    }
}
