package jacobfix.scorepredictor;

import java.util.Comparator;

public class PredictionComparator implements Comparator<Prediction> {

    private FullGame game;

    public PredictionComparator(FullGame game) {
        this.game = game;
    }

    @Override
    public int compare(Prediction p1, Prediction p2) {
        return p1.getSpread(game) - p2.getSpread(game);
    }
}
