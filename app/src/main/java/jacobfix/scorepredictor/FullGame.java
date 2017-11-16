package jacobfix.scorepredictor;

import org.json.JSONObject;

public class FullGame extends Game {

    private String clock;

    private DriveFeed driveFeed;

    public FullGame(Game game) {
        super(game);
    }

    public void updateFromFullJson(JSONObject json) {
        synchronized (lock) {
            /* Do update. */
        }
    }

    public String getClock() {
        return clock;
    }
}
