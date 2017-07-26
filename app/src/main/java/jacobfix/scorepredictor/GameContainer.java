package jacobfix.scorepredictor;

import java.util.ArrayList;

import jacobfix.scorepredictor.users.User;

public interface GameContainer {
    NflGame getGame();
    void registerGameStateChangeListener(GameStateChangeListener listener);
    void unregisterGameStateChangeListener(GameStateChangeListener listener);
    void notifyGameStateChanged();
    void notifyPredictionChanged();
}
