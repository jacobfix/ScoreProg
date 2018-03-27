package jacobfix.scoreprog;

import java.util.Comparator;

public class GameLobbyComparator implements Comparator<AtomicGame> {

    public int compare(AtomicGame g1, AtomicGame g2) {
        /* Order of precedence:
           predicted games,
           favorite teams,
           soonest game time (in progress precedes),

           For each item, split games into those that fulfill it and
           those that do not. Then repeat the process for each of the
           split segments.
         */
        return 0;
    }
}
