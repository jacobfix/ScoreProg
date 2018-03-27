package jacobfix.scoreprog.task;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.Future;

import jacobfix.scoreprog.ApplicationContext;

public abstract class BaseTask<T> implements Runnable {

    private static final String TAG = BaseTask.class.getSimpleName();

    // TODO: Add some kind of error recording component?
    private final int mTaskId;

    protected T mResult;

    private TaskFinishedListener mListener;
    private Future<?> mHandle;

    private boolean mInitiated;
    private boolean mRunning;
    private boolean mScheduled;

    // Error object
    private Exception mError;

    private static int globalTaskId = 0;

    public BaseTask() {
        mTaskId = ++globalTaskId;
        mRunning = false;
        mScheduled = false;
    }

    public BaseTask(TaskFinishedListener listener) {
        this();
        mListener = listener;
    }

    public int getTaskId() {
        return mTaskId;
    }

    public void start() {
        mRunning = true;
        TaskManager.getInstance().runTask(this);
    }

    public void schedule(long delay, long period) {
        mRunning = true;
        mScheduled = true;
        TaskManager.getInstance().scheduleTask(this, delay, period);
    }

    public void stop() {
        if (mRunning) {
            mHandle.cancel(true);
            TaskManager.getInstance().stopTask(this);
        }
        mRunning = false;
        mScheduled = false;
    }

    public void startOnThisThread() {
        mRunning = true;
        TaskManager.getInstance().runTaskInForeground(this);
    }

    public void setResult(T result) {
        mResult = result;
    }

    public T getResult() {
        return mResult;
    }

    public void setHandle(Future<?> handle) {
        mHandle = handle;
    }

    public void setListener(TaskFinishedListener listener) {
        mListener = listener;
    }

    public TaskFinishedListener getListener() {
        return mListener;
    }

    public void setRunning(boolean running) {
        mRunning = running;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public void reportError(Exception e) {
        Log.e(TAG, "Error reported: " + e.toString());
        mError = e;
    }

    public void reportError(Exception e, String where, TaskError type, String errorString) {
        Log.e(TAG, "Error reported!");
        Log.e(TAG, type + " in " + where + ": " + errorString);
        mError = e;
    }

    public boolean errorOccurred() {
        return mError != null;
    }

    public Exception getError() {
        return mError;
    }

    public void run() {
        // Maybe put "mRunning = true" here
        execute();
        if (!mScheduled)
            mRunning = false;
        notifyFinished();
    }

    private void notifyFinished() {
        Intent intent = new Intent(TaskManager.ACTION_ANNOUNCE_TASK_FINISHED);
        intent.putExtra(TaskManager.EXTRA_TASK_ID, mTaskId);
        LocalBroadcastManager.getInstance(ApplicationContext.getContext()).sendBroadcast(intent);
    }

    /* This method is to overridden by inheritors with the task. */
    public abstract void execute();

    public enum TaskError {
        JSON_ERROR,
        IO_ERROR,
        UNKNOWN_ERROR,
    }
}
