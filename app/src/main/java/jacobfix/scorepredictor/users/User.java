package jacobfix.scorepredictor.users;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jacobfix.scorepredictor.Prediction;

public class User {

    private static final String TAG = User.class.getSimpleName();

    private String mId;
    private String mUsername;
    private String mEmail;
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

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public Prediction getPrediction(String gameId) {
        return mPredictions.get(gameId);
    }

    public HashMap<String, Prediction> getPredictions() {
        return mPredictions;
    }

    public Set<String> getFriends() {
        return mFriends;
    }

    public void setFriends(Set<String> friendIds) {
        mFriends = friendIds;
    }

    public boolean isPlaying(String gameId) {
        return mPredictions.containsKey(gameId);
    }
}
