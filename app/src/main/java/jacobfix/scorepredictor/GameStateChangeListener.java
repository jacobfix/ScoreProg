package jacobfix.scorepredictor;

public interface GameStateChangeListener {
    void onGameStateChanged(Game game);
    void onPredictionChanged(Prediction prediction);
}
