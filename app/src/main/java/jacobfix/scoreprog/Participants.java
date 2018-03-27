package jacobfix.scoreprog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Participants {

    private String gameId;
    private Map<String, Prediction> predictions = new HashMap<>();

    private final Object lock = new Object();

    public Participants(String gameId) {
        this.gameId = gameId;
    }

    public String getGameId() {
        return gameId;
    }

    public void add(Prediction prediction) {
        predictions.put(prediction.getUserId(), prediction);
    }

    public void addAll(Collection<Prediction> predictions) {
        for (Prediction prediction : predictions)
            add(prediction);
    }

    public Set<String> getUserIds() {
        return predictions.keySet();
    }

    public Map<String, Prediction> getPredictions() {
        return predictions;
    }

    public Prediction getPrediction(String userId) {
        return predictions.get(userId);
    }

    public boolean isEmpty() {
        return predictions.isEmpty();
    }

    public Object acquireLock() {
        return lock;
    }
}
