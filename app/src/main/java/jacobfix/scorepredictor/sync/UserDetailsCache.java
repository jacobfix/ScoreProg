package jacobfix.scorepredictor.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SyncUserDetailsTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.UserDetails;

public class UserDetailsCache extends SyncableMap<String, UserDetails> {

    private HashSet<UserDetails> detailsToSync;

    @Override
    public SyncUserDetailsTask getSyncTask() {
        detailsToSync = new HashSet<>();
        return new SyncUserDetailsTask(detailsToSync, getTaskFinishedListener(scheduledSyncCallback));
    }

    @Override
    public String key(UserDetails userDetails) {
        return userDetails.getId();
    }

    public void sync(Collection<UserDetails> userDetails, AsyncCallback<ArrayList<UserDetails>> callback) {
        sync(userDetails, callback, false);
    }

    public void sync(Collection<UserDetails> userDetails, AsyncCallback<ArrayList<UserDetails>> callback, boolean foreground) {
        SyncUserDetailsTask task = new SyncUserDetailsTask(userDetails, getTaskFinishedListener(callback));
        if (foreground) task.startOnThisThread();
        else            task.start();
    }

    public void setUserDetailsToSync(UserDetails userDetails) {
        detailsToSync.clear();
        detailsToSync.add(userDetails);
    }

    public void setUserDetailsToSync(Collection<UserDetails> userDetails) {
        detailsToSync.clear();
        detailsToSync.addAll(userDetails);
    }
}
