package jacobfix.scoreprog;

import java.util.Comparator;

/* Requires external synchronization. */
public class PredictionComparator implements Comparator<Prediction> {

    private Game game;

    public PredictionComparator(Game game) {
        this.game = game;
    }

    @Override
    public int compare(Prediction p1, Prediction p2) {
        return p1.getSpread(game) - p2.getSpread(game);
    }
}
