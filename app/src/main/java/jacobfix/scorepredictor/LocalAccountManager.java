package jacobfix.scorepredictor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.users.UserDetails;

public class LocalAccountManager {

    private static LocalAccountManager instance;

    private UserDetails userDetails;
    private Friends friends;

    public static synchronized LocalAccountManager get() {
        if (instance == null)
            instance = new LocalAccountManager();
        return instance;
    }

    public void init(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public void setFriends(Friends friends) {
        this.friends = friends;
    }

    public String getId() {
        return userDetails.getId();
    }

    public Collection<String> getFriendIds() {
        return friends.getConfirmed();
    }

    public Friends getFriends() {
        return friends;
    }

    /*
    public static void set(UserDetails details) {
        userDetails = details;
    }

    public static String getId() {
        return userDetails.getId();
    }

    public static Collection<String> getFriends() {
        return userDetails.getFriendIds();
    }

    public static Collection<String> getFriendIds() {
        return userDetails.getFriendIds();
    }

    public static void setFriendIds(HashSet<String> friendIds) {
        userDetails.setFriendIds(friendIds);
    }
    */
}
