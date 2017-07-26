package jacobfix.scorepredictor;

import java.util.Collection;

import jacobfix.scorepredictor.users.User;

public class LocalAccountManager {

    private static User details;

    public static void set(User u) {
        details = u;
    }

    public static String getId() {
        return details.getId();
    }

    public static Collection<String> getFriends() {
        return details.getFriends();
    }
}
