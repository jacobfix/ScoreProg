package jacobfix.scoreprog.schedule;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.sync.SyncableCache;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.SyncScheduleTask;
import jacobfix.scoreprog.task.TaskFinishedListener;

public class ScheduleCache extends SyncableCache<String, ScheduledGame> {

    public void sync(int season, int week, int type, final AsyncCallback<ScheduledGame[]> listener) {
        new SyncScheduleTask(season, week, type, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    listener.onFailure(task.getError());
                    return;
                }

                ScheduledGame[] result = (ScheduledGame[]) task.getResult();
                for (ScheduledGame item : result)
                    set(item.gid, item);
                listener.onSuccess(result);
            }
        }).start();
    }

    public ScheduledGame[] syncInForeground(int season, int week, int type) {
        SyncScheduleTask task = new SyncScheduleTask(season, week, type, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    return;
                }

                ScheduledGame[] result = (ScheduledGame[]) task.getResult();
                for (ScheduledGame item : result)
                    set(item.gid, item);
            }
        });
        task.startOnThisThread();
        return task.getResult();
    }
}
