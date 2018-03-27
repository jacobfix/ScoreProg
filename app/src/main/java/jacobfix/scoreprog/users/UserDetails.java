package jacobfix.scoreprog.users;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;

public class UserDetails {

    private String id;
    private String username;

    private int friendStatus;

    public UserDetails() {

    }

    public UserDetails(String userId) {
        this.id = userId;
    }

    public String getUserId() {
        return id;
    }

    public void setUserId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
    }
}
