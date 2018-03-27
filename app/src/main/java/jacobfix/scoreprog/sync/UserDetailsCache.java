package jacobfix.scoreprog.sync;

import android.util.Log;

import java.util.Collection;
import java.util.HashSet;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.task.SyncUserDetailsTask;
import jacobfix.scoreprog.users.UserDetails;

public class UserDetailsCache extends SyncableMap<String, UserDetails> {

    private static final String TAG = UserDetailsCache.class.getSimpleName();

    private HashSet<UserDetails> detailsToSync;

    @Override
    public SyncUserDetailsTask getSyncTask() {
        detailsToSync = new HashSet<>();
        return new SyncUserDetailsTask(detailsToSync, getTaskFinishedListenerForCollection(scheduledSyncCallback));
    }

    @Override
    public String key(UserDetails userDetails) {
        return userDetails.getUserId();
    }

    public void sync(Collection<UserDetails> userDetails, AsyncCallback<Collection<UserDetails>> callback) {
        Log.d(TAG, "SYNCING!!");
        sync(userDetails, callback, false);
    }

    public void sync(Collection<UserDetails> userDetails, AsyncCallback<Collection<UserDetails>> callback, boolean foreground) {
        SyncUserDetailsTask task = new SyncUserDetailsTask(userDetails, getTaskFinishedListenerForCollection(callback));
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
