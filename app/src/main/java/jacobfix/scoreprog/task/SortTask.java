package jacobfix.scoreprog.task;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class SortTask<T> extends BaseTask<ArrayList<T>> {

    private static final String TAG = SortTask.class.getSimpleName();

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
        Log.d(TAG, "Before sorting: " + mItemsToSort.toString());
        for (T item : mItemsToSort) {
            insertOrdered(sorted, item, mComparator);
        }
        Log.d(TAG, "Finished sorting");
        Log.d(TAG, sorted.toString());
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
