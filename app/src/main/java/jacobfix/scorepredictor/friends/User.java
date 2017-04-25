package jacobfix.scorepredictor.friends;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jacobfix.scorepredictor.Prediction;

public class User {

    // TODO: Replace this class with User, or make a subclass of User if a reason to emerges
    private static final String TAG = User.class.getSimpleName();

    private String mId;
    private String mName;
    private HashMap<String, Prediction> mPredictions;
    private Set<String> mFriends;

    // TODO: Medals or something

    public User(String id) {
        mId = id;
        mFriends = new HashSet<>();
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public Prediction getPrediction(String gameId) {
        return mPredictions.get(gameId);
    }

    public Set<String> getFriends() {
        return mFriends;
    }

    public boolean isPlaying(String gameId) {
        return mPredictions.containsKey(gameId);
    }
}
