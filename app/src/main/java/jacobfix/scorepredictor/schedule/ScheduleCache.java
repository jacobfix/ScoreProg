package jacobfix.scorepredictor.schedule;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.sync.SyncableCache;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SyncScheduleTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;

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
