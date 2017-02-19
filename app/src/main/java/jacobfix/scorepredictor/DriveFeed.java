package jacobfix.scorepredictor;

import java.util.ArrayList;

public class DriveFeed {

    ArrayList<Drive> mDrives;
    ArrayList<Play> mPlayFeed;
    
    public DriveFeed() {
        mDrives = new ArrayList<Drive>();
        mPlayFeed = new ArrayList<Play>();
    }

    public Drive getDrive(int index) {
        return mDrives.get(index);
    }

    public void addDrive(Drive drive) {
        mDrives.add(drive);
        mPlayFeed.addAll(drive.getPlays());
    }

    public int numDrives() {
        return mDrives.size();
    }
    
    public ArrayList<Play> getPlayFeed() {
        return mPlayFeed;
    }
}
