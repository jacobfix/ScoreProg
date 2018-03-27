package jacobfix.scoreprog;

import java.util.ArrayList;

public class OriginalDriveFeed {

    ArrayList<OriginalDrive> mDrives;
    ArrayList<Play> mPlayFeed;
    
    public OriginalDriveFeed() {
        mDrives = new ArrayList<OriginalDrive>();
        mPlayFeed = new ArrayList<Play>();
    }

    public OriginalDrive getDrive(int index) {
        return mDrives.get(index);
    }

    public void addDrive(OriginalDrive drive) {
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
