package jacobfix.scoreprog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DriveFeed {

    private static final String TAG = DriveFeed.class.getSimpleName();

    private ArrayList<Drive> drives;

    private int lastJsonHash = 0;

    private final Object lock = new Object();

    public DriveFeed() {
        drives = new ArrayList<Drive>();
    }

    public void updateFromJson(JSONObject json) throws JSONException {
        int jsonHash = json.toString().hashCode();
        if (jsonHash == lastJsonHash)
            return;

        synchronized (lock) {
            int driveNum = 1;
            JSONObject driveJson;
            while ((driveJson = json.optJSONObject(String.valueOf(driveNum))) != null) {
                if (driveNum <= drives.size()) {
                    Drive drive = drives.get(driveNum - 1);
                    drive.updateFromJson(driveJson);
                } else {
                    Drive drive = new Drive();
                    drive.updateFromJson(driveJson);
                    drives.add(driveNum - 1, drive);
                }
                driveNum++;
            }

            lastJsonHash = jsonHash;
        }
    }

    public ArrayList<Play> allPlays() {
        ArrayList<Play> plays = new ArrayList<>();
        for (Drive d : drives) {
            synchronized (d.acquireLock()) {
                plays.addAll(d.plays());
            }
        }
        return plays;
    }

    public int numDrives() {
        return drives.size();
    }

    public Drive getDrive(int index) {
        return drives.get(index);
    }

    public void addDrive(int index, Drive drive) {
        drives.add(index, drive);
    }

    public Object acquireLock() {
        return lock;
    }
}
