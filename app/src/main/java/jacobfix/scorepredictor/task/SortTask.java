package jacobfix.scorepredictor.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * Returns an ArrayList of NflGames, sorted via insertion sort.
 */
public abstract class SortTask<T> extends BaseTask {

    private Collection<T> mItemsToSort;
    private Comparator mComparator;

    public SortTask(Collection<T> itemsToSort, Comparator comparator, TaskFinishedListener listener) {
        super(listener);
        mItemsToSort = itemsToSort;
        mComparator = comparator;
    }

    @Override
    public void execute() {
        ArrayList<T> sorted = new ArrayList<>();
        for (T item : mItemsToSort) {
            insertOrdered(sorted, item, mComparator);
        }
        mResult = sorted;
    }

    private void insertOrdered(ArrayList<T> sorted, T item, Comparator comparator) {
        for (int i = 0; i < sorted.size(); i++) {
            if (comparator.compare(item, sorted.get(i)) <= 0) {
                sorted.add(i, item);
                return;
            }
        }
        sorted.add(item);
    }
}
