package jacobfix.scorepredictor.task;

import java.util.Collection;

import jacobfix.scorepredictor.AtomicGame;
import jacobfix.scorepredictor.GameLobbyComparator;
import jacobfix.scorepredictor.NflGame;

/**
 * Returns a sorted ArrayList of the NflGames.
 */
public class SortGamesTask extends SortTask<AtomicGame> {

    public SortGamesTask(Collection<AtomicGame> gamesToSort, TaskFinishedListener listener) {
        super(gamesToSort, new GameLobbyComparator(), listener);
    }
}
