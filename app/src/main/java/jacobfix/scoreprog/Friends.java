package jacobfix.scoreprog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jacobfix.scoreprog.users.UserDetails;

public class Friends {

    private static final String TAG = Friends.class.getSimpleName();

    private Map<String, RelationDetails> users = new HashMap<>();

    public static final int NO_RELATION = 0;
    public static final int CONFIRMED = 1;
    public static final int PENDING_SENT = 2;
    public static final int PENDING_RECEIVED = 3;
    public static final int BLOCKED = 4;

    private final Object lock = new Object();

    public Set<RelationDetails> getConfirmedFriends() {
        Set<RelationDetails> friends = new HashSet<>();
        synchronized (lock) {
            for (RelationDetails relationDetails : users.values()) {
                if (relationDetails.getRelationStatus() == CONFIRMED)
                    friends.add(relationDetails);
            }
        }
        return friends;
    }

    public Set<RelationDetails> getPendingFriends() {
        Set<RelationDetails> pendingFriends = new HashSet<>();
        synchronized (lock) {
            for (RelationDetails relationDetails : users.values()) {
                if (relationDetails.getRelationStatus() == PENDING_SENT || relationDetails.getRelationStatus() == PENDING_RECEIVED)
                    pendingFriends.add(relationDetails);
            }
        }
        return pendingFriends;
    }

    public Set<RelationDetails> getBlockedUsers() {
        return new HashSet<>();
    }

    // TODO: Get rid of this, have users grab the RelationDetails object instead
    public int getRelationStatus(String userId) {
        RelationDetails relationDetails;

        synchronized (lock) {
            relationDetails = users.get(userId);
        }

        if (relationDetails == null)
            return NO_RELATION;

        return relationDetails.getRelationStatus();
    }

    public RelationDetails get(String userId) {
        synchronized (lock) {
            return users.get(userId);
        }
    }

    public Object acquireLock() {
        return lock;
    }

    public void addRelatedUser(RelationDetails relationDetails) {
        synchronized (lock) {
            users.put(relationDetails.getUserId(), relationDetails);
        }
    }

    public void removeRelatedUser(RelationDetails relationDetails) {
        synchronized (lock) {
            users.remove(relationDetails.getUserId());
        }
    }

    public void clear() {
        synchronized (lock) {
            users.clear();
        }
    }

    public static class Friend {
        int status;
        UserDetails user;

        public Friend(UserDetails user, int status) {
            this.user = user;
            this.status = status;
        }
    }
}
