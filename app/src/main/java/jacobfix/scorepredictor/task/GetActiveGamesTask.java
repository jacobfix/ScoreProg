package jacobfix.scorepredictor.task;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;

import jacobfix.scorepredictor.sync.JsonProvider;

public class GetActiveGamesTask extends BaseTask<String[]> {

    public GetActiveGamesTask(TaskFinishedListener listener) {
        super(listener);
    }

    @Override
    public void execute() {
        try {
            JSONArray json = JsonProvider.get().getActiveGamesJson();
            String[] result = new String[json.length()];
            for (int i = 0; i < json.length(); i++)
                result[i] = json.getString(i);
            setResult(result);
        } catch (IOException e) {
            reportError(e);
        } catch (JSONException e) {
            reportError(e);
        }
    }
}
