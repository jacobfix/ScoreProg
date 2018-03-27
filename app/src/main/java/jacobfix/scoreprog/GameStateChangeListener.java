package jacobfix.scoreprog;

public interface GameStateChangeListener {
    void onGameStateChanged(Game game);
    void onFullGameStateChanged(FullGame game);
    void onPredictionChanged(Prediction prediction);
}
