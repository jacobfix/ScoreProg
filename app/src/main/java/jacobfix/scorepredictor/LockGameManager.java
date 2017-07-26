package jacobfix.scorepredictor;

import android.os.SystemClock;
import android.util.Log;

import java.util.Comparator;
import java.util.TreeSet;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.util.NetUtil;

public class LockGameManager {

    private static final String TAG = LockGameManager.class.getSimpleName();

    private static LockGameManager instance;

    private long loadTimeMillis;
    private long loadTimeReferenceMillis;
    private final TreeSet<LockGameReference> lockGameReferences = new TreeSet<>(new LockGameComparator());

    private final String TIME_URL = "http://" + ApplicationContext.HOST + "/time.php";

    public static synchronized LockGameManager get() {
        if (instance == null)
            instance = new LockGameManager();
        return instance;
    }

    public void init() throws Exception {
        loadTimeMillis = Long.parseLong(NetUtil.makeGetRequest(TIME_URL));
        loadTimeReferenceMillis = SystemClock.elapsedRealtime();

        Log.d(TAG, "Load time millis: " + loadTimeMillis);
        Log.d(TAG, "Load time reference millis: " + loadTimeReferenceMillis);
        new ProcessTask().start();
    }

    public void scheduleLock(String gid, long locksAtMillis) {
        if (locksAtMillis < loadTimeMillis + (SystemClock.elapsedRealtime() - loadTimeReferenceMillis)) {
            Log.d(TAG, "Lock time has already passed. Don't have to queue the game for locking.");
            Schedule.lock(gid);
            return;
        }

        synchronized (lockGameReferences) {
            lockGameReferences.add(new LockGameReference(gid, locksAtMillis));
            Log.d(TAG, gid + " will expire at " + locksAtMillis);
            Log.d(TAG, "lockGameReferences size after add: " + lockGameReferences.size());
            lockGameReferences.notifyAll();
        }
    }

    public long now() {
        return loadTimeMillis + (SystemClock.elapsedRealtime() - loadTimeReferenceMillis);
    }

    class LoadTask extends BaseTask {

        @Override
        public void execute() {
            try {
                loadTimeMillis = Long.parseLong(NetUtil.makeGetRequest(TIME_URL));
                loadTimeReferenceMillis = SystemClock.elapsedRealtime();
            } catch (Exception e) {
                reportError(e);
            }
        }
    }

    class ProcessTask extends BaseTask {
        @Override
        public void execute() {
            Log.d(TAG, "Entering the ProcessTask loop");
            while (true) {
                LockGameReference gameToLock = null;

                synchronized (lockGameReferences) {
                    try {
                        while (lockGameReferences.isEmpty()) lockGameReferences.wait();

                        LockGameReference reference = lockGameReferences.first();

                        long now = loadTimeMillis + (SystemClock.elapsedRealtime() - loadTimeReferenceMillis);
                        if (reference.locksAtMillis < now) {
                            Log.d(TAG, reference.gid + " HAS EXPIRED");
                            Log.d(TAG, "Removing " + reference.gid + " (" + reference + ")");
                            gameToLock = reference;
                            lockGameReferences.remove(gameToLock);
                        } else {
                            Log.d(TAG, "Head not ready for removal. Waiting for " + (reference.locksAtMillis - (loadTimeMillis + (SystemClock.elapsedRealtime() - loadTimeReferenceMillis)) + " milliseconds."));
                            lockGameReferences.wait(reference.locksAtMillis - (loadTimeMillis + (SystemClock.elapsedRealtime() - loadTimeReferenceMillis)));
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.toString());
                    }
                }

                if (gameToLock != null)
                    Schedule.lock(gameToLock.gid);
            }
        }
    }

    private static class LockGameReference {

        private final String gid;
        private final long locksAtMillis;

        private LockGameReference(String id, long millis) {
            gid = id;
            locksAtMillis = millis;
        }

        @Override
        public boolean equals(Object other) {
            LockGameReference that = (LockGameReference) other;
            return this.gid.equals(that.gid) && this.locksAtMillis == that.locksAtMillis;
        }
    }

    private static class LockGameComparator implements Comparator<LockGameReference> {
        @Override
        public int compare(LockGameReference r1, LockGameReference r2) {
            if (r1.locksAtMillis < r2.locksAtMillis)                      return -1;
            else if (r1.locksAtMillis > r2.locksAtMillis)                 return 1;
            else if (Integer.parseInt(r1.gid) < Integer.parseInt(r2.gid)) return -1;
            else if (Integer.parseInt(r1.gid) > Integer.parseInt(r2.gid)) return 1;
            else                                                          return 0;
        }
    }
}
