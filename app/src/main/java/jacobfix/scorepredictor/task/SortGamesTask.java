package jacobfix.scorepredictor.task;

import java.util.Collection;

import jacobfix.scorepredictor.GameLobbyComparator;
import jacobfix.scorepredictor.NflGame;

/**
 * Returns a sorted ArrayList of the NflGames.
 */
public class SortGamesTask extends SortTask<NflGame> {

    public SortGamesTask(Collection<NflGame> gamesToSort, TaskFinishedListener listener) {
        super(gamesToSort, new GameLobbyComparator(), listener);
    }
}
