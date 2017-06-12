package jacobfix.scorepredictor.task;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import jacobfix.scorepredictor.server.JsonParser;
import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.server.NflGameJsonRetriever;

/**
 * Takes in a list of NflGames and syncs these games with the server.
 */
public class SyncNflGamesTask extends BaseTask<HashMap<String, NflGame>> {

    private static final String TAG = SyncNflGamesTask.class.getSimpleName();

    private Collection<NflGame> mGamesToSync;
    private NflGameJsonRetriever mJsonRetriever;
    private JsonParser mJsonParser = new JsonParser();

    public SyncNflGamesTask(Collection<NflGame> games, NflGameJsonRetriever retriever, TaskFinishedListener listener) {
        super(listener);
        mGamesToSync = games;
        mJsonRetriever = retriever;
    }

    @Override
    public void execute() {
        for (NflGame game : mGamesToSync) {
            try {
                JSONObject json = mJsonRetriever.getIndividualGameJson(game.getGameId());

                mJsonParser.updateGameFromJson(json, game);
                /* Lock down the NflGame object before modifying it. */
                /* synchronized (game) {
                    mJsonParser.updateGameFromJson(json, game);
                } */
                Log.d(TAG, "Task " + getTaskId() + ": Sync'd " + game.getGameId());

            } catch (JSONException e) {
                Log.e(TAG, "REPORTING JSON ERROR!!!!!");
                reportError(e, TAG, TaskError.JSON_ERROR, e.toString());
            } catch (IOException e) {
                Log.e(TAG, "REPORTING IO ERROR!!!!");
                reportError(e, TAG, TaskError.IO_ERROR, e.toString());
            } catch (Exception e) {
                Log.e(TAG, "REPORTING GENERAL ERROR!!!!");
                reportError(e, TAG, TaskError.UNKNOWN_ERROR, e.toString());
            }
        }
    }
}
