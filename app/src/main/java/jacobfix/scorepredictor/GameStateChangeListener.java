package jacobfix.scorepredictor;

public interface GameStateChangeListener {
    void onGameStateChanged(NflGame game);
    void onPredictionChanged(Prediction prediction);
}
