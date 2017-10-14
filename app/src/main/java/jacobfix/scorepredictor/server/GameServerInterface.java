package jacobfix.scorepredictor.server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;

import jacobfix.scorepredictor.util.NetUtil;

public abstract class GameServerInterface {

    public static GameServerInterface getDefault() {
        return traditional;
    }

    public abstract JSONObject getFullGameJson(String gameId) throws IOException, JSONException;

    private static GameServerInterface traditional = new GameServerInterface() {

        String fullGameUrl = "http://thefixhome.com/sp/games/%s";

        @Override
        public JSONObject getFullGameJson(String gameId)  throws IOException, JSONException {
            try {
                return new JSONObject(NetUtil.makeGetRequest(String.format(fullGameUrl, gameId)));
            } catch (FileNotFoundException e) {
                return null;
            }
        }
    };

    private static GameServerInterface test = new GameServerInterface() {
        @Override
        public JSONObject getFullGameJson(String gameId) {
            return null;
        }
    };
}
