package jacobfix.scorepredictor;

import java.util.HashMap;

public class NflGameOracle {

    private HashMap<String, NflGame> mActiveGames;
    private HashMap<String, NflGame> archivedGames;

    private static NflGameOracle instance;
    
    public NflGameOracle() {
        mActiveGames = new HashMap<String, NflGame>();
    }

    public static NflGameOracle getInstance() {
        if (instance == null) {
            instance = new NflGameOracle();
        }
        return instance;
    }

    public boolean isActiveGame(String gameId) {
        return this.mActiveGames.containsKey(gameId);
    }

    public NflGame getActiveGame(String gameId) {
        if (isActiveGame(gameId)) {
            return this.mActiveGames.get(gameId);
        }
        return null;
    }

    public void setActiveGames(HashMap<String, NflGame> games) {
        this.mActiveGames = games;
    }

    public HashMap<String, NflGame> getActiveGames() {
        return this.mActiveGames;
    }
}
