package jacobfix.scoreprog;

public class GameDetails {

    public boolean locked;
    public String awayAbbr;
    public String homeAbbr;
    public String awayName;
    public String homeName;
    public long startTime;
    public String clock;
    public int quarter;
    public int awayScore;
    public int homeScore;
    Game.State state;

    public static GameDetails capture(Game game) {
        GameDetails gameDetails = new GameDetails();
        synchronized (game.acquireLock()) {
            gameDetails.locked = game.isLocked();
            gameDetails.awayAbbr = game.getAwayAbbr();
            gameDetails.homeAbbr = game.getHomeAbbr();
            gameDetails.awayName = game.getAwayName();
            gameDetails.homeName = game.getHomeName();
            gameDetails.awayScore = game.getAwayScore();
            gameDetails.homeScore = game.getHomeScore();
            gameDetails.startTime = game.getStartTime();
            if (game.isPregame()) {
                gameDetails.state = Game.State.PREGAME;
            } else if (game.isFinal()) {
                gameDetails.state = Game.State.FINAL;
            } else {
                gameDetails.state = Game.State.IN_PROGRESS;
            }
            gameDetails.clock = game.getClock();
            gameDetails.quarter = game.getQuarter();
        }
        return gameDetails;
    }
}
