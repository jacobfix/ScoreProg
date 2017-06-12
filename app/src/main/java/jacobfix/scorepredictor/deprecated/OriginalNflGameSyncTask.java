package jacobfix.scorepredictor.deprecated;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import jacobfix.scorepredictor.NflGame;
import jacobfix.scorepredictor.sync.NflGameOracle;
import jacobfix.scorepredictor.NflTeam;
import jacobfix.scorepredictor.database.Database;

public class OriginalNflGameSyncTask implements Runnable {

    private static final String TAG = OriginalNflGameSyncTask.class.getSimpleName();

    private Context mContext;
    private DataRetriever mDataRetriever;
    private Database mDatabase;
    private boolean mFirstSync = true;

    public OriginalNflGameSyncTask(Context context, DataRetriever dataRetriever) {
        mContext = context;
        mDataRetriever = dataRetriever;
        mDatabase = new Database(context);
    }

    public void setDataRetriever(DataRetriever retriever) {
        mDataRetriever = retriever;
    }

    public void setFirstSync(boolean first) {
        mFirstSync = first;
    }

    @Override
    public void run() {
        // synchronized?
        HashMap<String, NflGame> existingGames = NflGameOracle.getInstance().getActiveGames();
        HashMap<String, NflGame> activeGames = new HashMap<String, NflGame>();
        syncWithServer(existingGames, activeGames);
        mDatabase.open();
        if (mFirstSync) {
            syncWithLocalDatabase(activeGames);
        }
        mFirstSync = false;
        updateDatabase(activeGames); // add new active games to database
        mDatabase.close();
        NflGameOracle.getInstance().setActiveGames(activeGames);
        Intent intent = new Intent(NflGameSyncManager.ACTION_ANNOUNCE_SYNC_FINISHED);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void syncWithServer(HashMap<String, NflGame> existingGames, HashMap<String, NflGame> activeGames) {
        JSONObject allGamesJson = null;
        try {
            allGamesJson = mDataRetriever.getAllGamesJson();
        } catch (IOException e) {
            Log.e(TAG, "There was an IO error " + e.toString());
        } catch (JSONException e) {
            Log.e(TAG, "There was a JSON error " + e.toString());
        }

        Iterator<String> iter = allGamesJson.keys();
        while (iter.hasNext()) {
            String gameId = iter.next();
            JSONObject simpleJson = null;
            try {
                simpleJson = allGamesJson.getJSONObject(gameId);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }

            NflGame game = null;
            if (existingGames.containsKey(gameId)) {
                game = existingGames.get(gameId);
            } else {
                game = new NflGame(gameId);
                game.setAwayTeam(new NflTeam(false));
                game.setHomeTeam(new NflTeam(true));
                // activeGames.put(gameId, game);
            }
            activeGames.put(gameId, game);

            // Want to maintain the existing object?
            /*
            NflGame game = new NflGame(gameId);
            try {
                game.setAwayTeam(new NflTeam(OriginalNflGameJsonParser.getAwayTeamAbbr(simpleJson)));
                game.setHomeTeam(new NflTeam(OriginalNflGameJsonParser.getHomeTeamAbbr(simpleJson)));
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
            */

            try {
                OriginalNflGameJsonParser.updateGameFromSimpleJson(game, simpleJson);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }

            if (game.isPregame()) { // or isFinal()?
                continue;
            }

            // String url = String.format(SINGLE_GAME_BASE_URL, gameId, gameId);
            JSONObject detailedJson = null;
            try {
                detailedJson = mDataRetriever.getIndividualGameJson(gameId).getJSONObject(gameId);
                // detailedJson = OriginalNflGameJsonParser.getJsonFromUrl(url).getJSONObject(gameId);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }

            try {
                OriginalNflGameJsonParser.updateGameFromDetailedJson(game, detailedJson);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    private void syncWithLocalDatabase(HashMap<String, NflGame> activeGames) {
        for (String gameId : activeGames.keySet()) {
            mDatabase.syncGameIfExists(activeGames.get(gameId));
        }
    }

    private void updateDatabase(HashMap<String, NflGame> activeGames) {
        for (String gameId : activeGames.keySet()) {
            if (!mDatabase.gameExists(gameId)) {
                mDatabase.createEntry(activeGames.get(gameId));
            }
        }
    }
}
