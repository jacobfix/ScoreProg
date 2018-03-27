package jacobfix.scoreprog.sync;

import java.util.Collection;
import java.util.HashSet;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.SyncDetailsTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.users.User;

public class DetailsCache extends SyncableCache<String, User> {

    private HashSet<User> detailsToSync = new HashSet<User>();

    public DetailsCache() {
        syncTask = new SyncDetailsTask(detailsToSync, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    notifyAllOfFailure(task.getError());
                    return;
                }

                User[] result = (User[]) task.getResult();
                for (User user : result)
                    set(user.getId(), user);
                notifyAllOfSuccess(result);
            }
        });
    }

    public void sync(Collection<User> requested, final AsyncCallback<User[]> listener) {
        new SyncDetailsTask(requested, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    listener.onFailure(task.getError());
                    return;
                }

                User[] result = (User[]) task.getResult();
                for (User user : result)
                    set(user.getId(), user);
                listener.onSuccess(result);
            }
        }).start();
    }

    public void setDetailsToSync(Collection<User> users) {
        detailsToSync.clear();
        detailsToSync.addAll(users);
    }

    public void addDetailsToSync(User user) {
        detailsToSync.add(user);
    }

    public void clearDetailsToSync() {
        detailsToSync.clear();
    }
}
