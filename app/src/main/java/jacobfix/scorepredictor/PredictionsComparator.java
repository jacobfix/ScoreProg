package jacobfix.scorepredictor;

import java.util.Comparator;

public class PredictionsComparator implements Comparator<Prediction> {

    private AtomicGame game;

    public PredictionsComparator(AtomicGame game) {
        this.game = game;
    }

    /*
    @Override
    public int compare(Prediction p1, Prediction p2) {
        return p1.get(game.getId()).getSpread(game) - p2.get(game.getId()).getSpread(game);
    }
    */

    @Override
    public int compare(Prediction p1, Prediction p2) {

        return 0;
    }
}
