package jacobfix.scorepredictor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * Returns a sorted ArrayList of the NflGames.
 */
public class SortGamesTask extends SortTask<NflGame> {

    public SortGamesTask(Collection<NflGame> gamesToSort, TaskFinishedListener listener) {
        super(gamesToSort, new GameLobbyComparator(), listener);
    }
}
