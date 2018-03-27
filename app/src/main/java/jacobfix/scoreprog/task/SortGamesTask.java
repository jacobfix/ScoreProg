package jacobfix.scoreprog.task;

import java.util.Collection;

import jacobfix.scoreprog.AtomicGame;
import jacobfix.scoreprog.GameLobbyComparator;

/**
 * Returns a sorted ArrayList of the NflGames.
 */
public class SortGamesTask extends SortTask<AtomicGame> {

    public SortGamesTask(Collection<AtomicGame> gamesToSort, TaskFinishedListener listener) {
        super(gamesToSort, new GameLobbyComparator(), listener);
    }
}
