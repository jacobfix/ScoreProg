package jacobfix.scoreprog;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class Predictions {

    private boolean indexByUser;
    private HashMap<String, LinkedList<Prediction>> contents;

    public Predictions(boolean indexByUser) {
        this.indexByUser = indexByUser;
        this.contents = new HashMap<String, LinkedList<Prediction>>();
    }

    public void put(Prediction prediction) {
        String index = (indexByUser) ? prediction.getUserId() : prediction.getGameId();
        LinkedList<Prediction> predictions = contents.get(index);
        if (predictions == null) {
            predictions = new LinkedList<Prediction>();
            contents.put(index, predictions);
        }
        predictions.add(prediction);
    }

    public void putAll(Collection<Prediction> all) {
        for (Prediction p : all)
            put(p);
    }

    public LinkedList<Prediction> get(String key) {
        return contents.get(key);
    }

    public LinkedList<Prediction> getAll() {
        Collection<LinkedList<Prediction>> allLists = contents.values();

        LinkedList<Prediction> result = new LinkedList<>();
        for (LinkedList<Prediction> list : allLists)
            result.addAll(list);
        return result;
    }

    public Set<String> keys() {
        return contents.keySet();
    }

    public boolean getIndexByUser() {
        return indexByUser;
    }
}
