package jacobfix.scorepredictor.users;

import java.util.Collection;
import java.util.HashSet;

public class UserDetails {

    private String id;
    private String username;
    private String email;
    HashSet<String> friends = new HashSet<>();

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

    public Collection<String> getFriends() {
        return friends;
    }
}
