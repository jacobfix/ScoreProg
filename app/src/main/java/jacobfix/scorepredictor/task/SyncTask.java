package jacobfix.scorepredictor.task;

import jacobfix.scorepredictor.sync.Syncable;
import jacobfix.scorepredictor.sync.SyncableCache;

public abstract class SyncTask<T extends Syncable> extends BaseTask {

    public SyncTask(SyncableCache c) {

    }

    @Override
    public void execute() {
        T result = sync();

    }

    public abstract T sync();
}
