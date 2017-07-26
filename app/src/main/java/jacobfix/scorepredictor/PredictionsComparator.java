package jacobfix.scorepredictor;

import java.util.Comparator;

public class PredictionsComparator implements Comparator<Predictions> {

    private NflGame game;

    public PredictionsComparator(NflGame g) {
        game = g;
    }

    @Override
    public int compare(Predictions p1, Predictions p2) {
        return p1.get(game.getGameId()).getSpread(game) - p2.get(game.getGameId()).getSpread(game);
    }
}
