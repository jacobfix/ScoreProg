package jacobfix.scorepredictor.task;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Collection;

import jacobfix.scorepredictor.server.JsonParser;
import jacobfix.scorepredictor.server.NflGameJsonRetriever;

/**
 * Retrieves a list of game IDs corresponding to the games which are currently active.
 */
public class OriginalGetActiveGamesTask extends BaseTask<Collection<String>> {

    private static final String TAG = OriginalGetActiveGamesTask.class.getSimpleName();

    NflGameJsonRetriever mJsonRetriever;
    JsonParser mJsonParser = new JsonParser();

    public OriginalGetActiveGamesTask(NflGameJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        super(listener);
        mJsonRetriever = jsonRetriever;
    }

    @Override
    public void execute() {
        Log.d(TAG, "Task " + getTaskId() + ": Executing OriginalGetActiveGamesTask");
        try {
            JSONArray json = mJsonRetriever.getActiveGamesJson();
            mResult = mJsonParser.parseActiveGamesJson(json);
        } catch (JSONException e) {
            reportError(e, TAG, TaskError.JSON_ERROR, e.toString());
        } catch (IOException e) {
            reportError(e, TAG, TaskError.IO_ERROR, e.toString());
        } catch (Exception e) {
            reportError(e, TAG, TaskError.UNKNOWN_ERROR, e.toString());
        }
    }
}
