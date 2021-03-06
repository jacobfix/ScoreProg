package jacobfix.scoreprog;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Comparator;
import java.util.TreeSet;

import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.server.ScheduleServerInterface;
import jacobfix.scoreprog.sync.GameProvider;
import jacobfix.scoreprog.task.BaseTask;

public class LockGameManager {

    private static final String TAG = LockGameManager.class.getSimpleName();

    private static LockGameManager instance;

    private long loadTimeMillis;
    private long loadTimeReferenceMillis;
    private final TreeSet<LockGameReference> lockGameReferences = new TreeSet<>(new LockGameComparator());

    // private final String TIME_URL = "http://" + ApplicationContext.HOST + "/time.php";

    public static synchronized LockGameManager get() {
        if (instance == null)
            instance = new LockGameManager();
        return instance;
    }

    public void init() throws Exception {
        // loadTimeMillis = Long.parseLong(NetUtil.makeGetRequest(TIME_URL));
        loadTimeMillis = ScheduleServerInterface.getDefault().currentTimeMillis();
        loadTimeReferenceMillis = SystemClock.elapsedRealtime();

        Log.d(TAG, "Load time millis: " + loadTimeMillis);
        Log.d(TAG, "Load time reference millis: " + loadTimeReferenceMillis);
        new ProcessTask().start();
    }

    public void scheduleLock(String gid, long locksAtMillis) {
        if (locksAtMillis < loadTimeMillis + (SystemClock.elapsedRealtime() - loadTimeReferenceMillis)) {
            Log.d(TAG, "(" + gid + ") Lock time has already passed. Don't have to queue the game for locking.");
            Schedule.lock(gid);

//            Intent intent = new Intent(GameActivity.ACTION_ANNOUNCE_GAME_LOCKED);
//            intent.putExtra("game_id", gid);
//            LocalBroadcastManager.getInstance(ApplicationContext.getContext()).sendBroadcast(intent);
            return;
        }

        Log.d(TAG, "We have one scheduled! (" + gid + ")");
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
                loadTimeMillis = ScheduleServerInterface.getDefault().currentTimeMillis();
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
                        Log.d(TAG, "About to check if lockGameReferences is empty");
                        while (lockGameReferences.isEmpty()) lockGameReferences.wait();

                        LockGameReference reference = lockGameReferences.first();

                        long now = loadTimeMillis + (SystemClock.elapsedRealtime() - loadTimeReferenceMillis);
                        Log.d(TAG, "Locks at: " + reference.locksAtMillis + ", Now: " + now);
                        if (reference.locksAtMillis <= now) {
                            Log.d(TAG, reference.gid + " HAS EXPIRED");
                            Log.d(TAG, "Removing " + reference.gid + " (" + reference + ")");
                            gameToLock = reference;

                            Schedule.lock(gameToLock.gid);

                            Intent intent = new Intent(ApplicationContext.ACTION_ANNOUNCE_GAME_LOCKED);
                            intent.putExtra("gameId", gameToLock.gid);
                            LocalBroadcastManager.getInstance(ApplicationContext.getContext()).sendBroadcast(intent);

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
            if (r1.locksAtMillis < r2.locksAtMillis)                  return -1;
            else if (r1.locksAtMillis > r2.locksAtMillis)             return 1;
            else if (Long.parseLong(r1.gid) < Long.parseLong(r2.gid)) return -1;
            else if (Long.parseLong(r1.gid) > Long.parseLong(r2.gid)) return 1;
            else                                                      return 0;
        }
    }
}
