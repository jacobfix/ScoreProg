package jacobfix.scorepredictor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jacobfix.scorepredictor.util.NetUtil;

public class GameRetriever {

    private static String GAME_BASE_URL = "http://www.nfl.com/liveupdate/game-center/%s/%s_gtd.json";

    public static JSONObject getGameJson(String gid) throws IOException, JSONException {
        try {
            String response = NetUtil.makeGetRequest(String.format(GAME_BASE_URL, gid, gid));
            return new JSONObject(response).getJSONObject(gid);
        } catch (Exception e) {
            return null;
        }
    }
}
