package jacobfix.scorepredictor.server;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class NflWebsiteDataRetriever extends DataRetriever {

    private static final String ALL_GAMES_URL = "http://www.nfl.com/liveupdate/scores/scores.json";
    private static final String SINGLE_GAME_BASE_URL = "http://www.nfl.com/liveupdate/game-center/%s/%s_gtd.json";

    public NflWebsiteDataRetriever(Context context) {
        super(context);
    }

    @Override
    public JSONObject getAllGamesJson() throws IOException, JSONException {
        return getJsonFromUrl(ALL_GAMES_URL);
    }

    @Override
    public JSONObject getIndividualGameJson(String gameId) throws IOException, JSONException {
        return getJsonFromUrl(String.format(SINGLE_GAME_BASE_URL, gameId, gameId));
    }
}
