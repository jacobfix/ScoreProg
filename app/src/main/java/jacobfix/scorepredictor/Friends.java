package jacobfix.scorepredictor;

import org.json.JSONObject;

import java.util.LinkedList;

public class Friends {

    private LinkedList<String> confirmed = new LinkedList<>();
    private LinkedList<String> pending = new LinkedList<>();
    private LinkedList<String> blocked = new LinkedList<>();

    private final Object lock = new Object();

    public LinkedList<String> getConfirmed() {
        return confirmed;
    }

    public LinkedList<String> getPending() {
        return pending;
    }

    public LinkedList<String> getBlocked() {
        return blocked;
    }

    public void addConfirmed(String userId) {
        confirmed.add(userId);
    }

    public void addPending(String userId) {
        pending.add(userId);
    }

    public void addBlocked(String userId) {
        blocked.add(userId);
    }

    public Object getLock() {
        return lock;
    }
}
