package jacobfix.scorepredictor;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import jacobfix.scorepredictor.server.NflGameJsonRetriever;

/**
 * Retrieves a list of game IDs corresponding to the games which are currently active.
 */
public class GetActiveGamesTask extends BaseTask<Collection<String>> {

    private static final String TAG = GetActiveGamesTask.class.getSimpleName();

    NflGameJsonRetriever mJsonRetriever;
    NflGameJsonParser mJsonParser = new NflGameJsonParser();

    public GetActiveGamesTask(NflGameJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        super(listener);
        mJsonRetriever = jsonRetriever;
    }

    @Override
    public void execute() {
        Log.d(TAG, "Task " + getTaskId() + ": Executing GetActiveGamesTask");
        try {
            JSONArray json = mJsonRetriever.getActiveGamesJson();
            mResult = mJsonParser.parseActiveGamesJson(json);
        } catch (JSONException e) {
            reportError(TaskError.JSON_ERROR, e.toString());
        } catch (IOException e) {
            reportError(TaskError.IO_ERROR, e.toString());
        } catch (Exception e) {
            reportError(TaskError.UNKNOWN_ERROR, e.toString());
        }
    }
}
