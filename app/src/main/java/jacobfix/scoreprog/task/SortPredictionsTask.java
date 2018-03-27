package jacobfix.scoreprog.task;

import java.util.Collection;
import java.util.Comparator;

import jacobfix.scoreprog.Prediction;

public class SortPredictionsTask extends SortTask<Prediction> {

    public SortPredictionsTask(Collection<Prediction> itemsToSort, Comparator comparator, TaskFinishedListener listener) {
        super(itemsToSort, comparator, listener);
    }
}
