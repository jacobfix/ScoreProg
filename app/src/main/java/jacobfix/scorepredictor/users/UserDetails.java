package jacobfix.scorepredictor.users;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;

public class UserDetails {

    private String id;
    private String username;
    private String email;
    HashSet<String> friendIds = new HashSet<>();

    public UserDetails(String userId) {
        id = userId;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Collection<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(HashSet<String> friendIds) {
        this.friendIds = friendIds;
    }

    public void sync(JSONObject json) throws JSONException {
        synchronized (this) {
            username = json.getString("username");
            email = json.getString("email");
        }
    }
}
