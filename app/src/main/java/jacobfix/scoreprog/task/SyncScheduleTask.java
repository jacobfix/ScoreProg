package jacobfix.scoreprog.task;

import android.util.Log;

import jacobfix.scoreprog.schedule.ScheduleRetriever;
import jacobfix.scoreprog.schedule.ScheduledGame;

public class SyncScheduleTask extends BaseTask<ScheduledGame[]> {

    private static final String TAG = SyncScheduleTask.class.getSimpleName();

    private int season;
    private int week;
    private int type;

    public SyncScheduleTask(int s, int w, int t, TaskFinishedListener listener) {
        super(listener);
        season = s;
        week = w;
        type = t;
    }

    @Override
    public void execute() {
        try {
            Log.d(TAG, "Running SyncScheduleTask");
            setResult(ScheduleRetriever.get().getWeek(season, week, type));
        } catch (Exception e) {
            reportError(e);
        }
    }
}
