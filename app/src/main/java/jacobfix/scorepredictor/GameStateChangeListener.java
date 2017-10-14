package jacobfix.scorepredictor;

public interface GameStateChangeListener {
    void onGameStateChanged(FullGame game);
    void onPredictionChanged(Prediction prediction);
}
