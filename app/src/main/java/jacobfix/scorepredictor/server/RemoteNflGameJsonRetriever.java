package jacobfix.scorepredictor.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RemoteNflGameJsonRetriever extends NflGameJsonRetriever {

    private static final String TAG = RemoteNflGameJsonRetriever.class.getSimpleName();

    // private static final String ACTIVE_GAMES_URL = "http://192.168.1.15/active";
    // private static final String GAME_BASE_URL = "http://192.168.1.15/games/%s.json";

    private static final String ACTIVE_GAMES_URL = "http://172.20.8.156/active";
    private static final String GAME_BASE_URL = "http://172.20.8.156/games/%s.json";

    /*
    private static final String ALL_GAMES_URL = "http://www.nfl.com/liveupdate/scores/scores.json";
    private static final String SINGLE_GAME_BASE_URL = "http://www.nfl.com/liveupdate/game-center/%s/%s_gtd.json";
    */

    // TODO: Should this class handle hashing the data to check if it needs to be retransmitted or not?

    @Override
    public JSONArray getActiveGamesJson() throws JSONException, IOException {
        return getJsonArrayFromUrl(ACTIVE_GAMES_URL);
    }

    @Override
    public JSONObject getIndividualGameJson(String gameId) throws JSONException, IOException {
        return getJsonObjectFromUrl(String.format(GAME_BASE_URL, gameId));
    }
}
