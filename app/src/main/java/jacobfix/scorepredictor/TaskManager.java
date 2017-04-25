package jacobfix.scorepredictor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskManager {

    private static final String TAG = TaskManager.class.getSimpleName();

    private static TaskManager instance;

    private ExecutorService mExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private ScheduledExecutorService mScheduledExecutor = Executors.newScheduledThreadPool(SCHEDULED_THREAD_POOL_SIZE);

    private BroadcastReceiver mTaskFinishedReceiver;

    /* Map used to recover task objects after they have finished running. */
    private HashMap<Integer, BaseTask> mRunningTasks;

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String ACTION_ANNOUNCE_TASK_FINISHED = "task_fin";

    private static final int THREAD_POOL_SIZE = 5;
    private static final int SCHEDULED_THREAD_POOL_SIZE = 5;

    public static synchronized TaskManager getInstance() {
        if (instance == null) {
            // TODO: Make up your mind about how to get application context
            instance = new TaskManager(ApplicationContext.getContext());
        }
        return instance;
    }

    public TaskManager(Context context) {
        mRunningTasks = new HashMap<Integer, BaseTask>();
        mTaskFinishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int taskId = intent.getIntExtra(EXTRA_TASK_ID, 0);
                BaseTask task = mRunningTasks.get(taskId);
                if (!task.isRunning()) {
                    mRunningTasks.remove(taskId);
                }
                Log.d(TAG, String.format("Task %d finished, notifying listener %s", task.getTaskId(), task.getListener() == null ? "null" : task.getListener().getClass().getSimpleName()));
                /* Task may or may not have a listener to notify when it completes. */
                if (task.getListener() != null) {
                    task.getListener().onTaskFinished(task);
                }
            }
        };
        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(mTaskFinishedReceiver, new IntentFilter(ACTION_ANNOUNCE_TASK_FINISHED));
    }

    public void runTask(BaseTask task) {
        mRunningTasks.put(task.getTaskId(), task);
        task.setHandle(mExecutor.submit(task));
    }

    public void scheduleTask(BaseTask task, long delay, long period) {
        mRunningTasks.put(task.getTaskId(), task);
        task.setHandle(mScheduledExecutor.scheduleAtFixedRate(task, delay, period, TimeUnit.SECONDS));
    }
}
