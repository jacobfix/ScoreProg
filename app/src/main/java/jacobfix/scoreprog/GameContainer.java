package jacobfix.scoreprog;

public interface GameContainer {
    NflGame getGame();
    void registerGameStateChangeListener(GameStateChangeListener listener);
    void unregisterGameStateChangeListener(GameStateChangeListener listener);
    void notifyGameStateChanged();
    void notifyPredictionChanged();
}
