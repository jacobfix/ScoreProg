package jacobfix.scoreprog.sync;

import jacobfix.scoreprog.DriveFeed;
import jacobfix.scoreprog.Game;

public class GameAndDriveFeed {

    public Game game;
    public DriveFeed driveFeed;

    public GameAndDriveFeed(Game game, DriveFeed driveFeed) {
        this.game = game;
        this.driveFeed = driveFeed;
    }
}
