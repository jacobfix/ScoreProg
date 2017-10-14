package jacobfix.scorepredictor.task;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobfix.scorepredictor.Friends;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SyncFriendsTaskTest {

    @Test
    public void testOne() {
        new SyncFriendsTask("1", new TaskFinishedListener<SyncFriendsTask>() {
            @Override
            public void onTaskFinished(SyncFriendsTask task) {
                if (task.errorOccurred()) {
                    assertTrue(false);
                    return;
                }

                Friends result = task.getResult();
                assertEquals(result.getConfirmed().size(), 3);
                assertEquals(result.getPending().size(), 1);
                assertEquals(result.getBlocked().size(), 0);
            }
        }).startOnThisThread();
    }
}