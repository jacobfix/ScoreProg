package jacobfix.scoreprog.task;

public interface TaskFinishedListener<T extends BaseTask> {

    void onTaskFinished(T task);
}
