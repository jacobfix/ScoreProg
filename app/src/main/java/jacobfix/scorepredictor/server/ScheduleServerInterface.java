package jacobfix.scorepredictor.server;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jacobfix.scorepredictor.util.NetUtil;
import jacobfix.scorepredictor.util.Util;

public abstract class ScheduleServerInterface {

    private static final String TAG = ScheduleServerInterface.class.getSimpleName();

    public static ScheduleServerInterface getDefault() {
        return traditional;
    }

    public abstract long currentTimeMillis() throws IOException;
    public abstract JSONObject getCurrentSeasonStateJson() throws IOException, JSONException;
    public abstract JSONObject getFullSeasonSchedule(int year) throws IOException, JSONException;

    private static ScheduleServerInterface traditional = new ScheduleServerInterface() {

        String currentSeasonStateUrl = "http://thefixhome.com/sp/schedule/current";
        String fullSeasonScheduleUrl = "http://thefixhome.com/sp/schedule/%d/full";

        String millisTimeUrl = "http://thefixhome.com/sp/time.php";

        @Override
        public long currentTimeMillis() throws IOException {
            return Long.parseLong(NetUtil.makeGetRequest(millisTimeUrl));
        }

        @Override
        public JSONObject getCurrentSeasonStateJson() throws IOException, JSONException {
            Log.d(TAG, "Getting current season state");
            String response = NetUtil.makeGetRequest(currentSeasonStateUrl);
            return new JSONObject(response);
        }

        @Override
        public JSONObject getFullSeasonSchedule(int year) throws IOException, JSONException {
            String response = NetUtil.makeGetRequest(String.format(fullSeasonScheduleUrl, year));
            return new JSONObject(response);
        }
    };

    private static ScheduleServerInterface local = new ScheduleServerInterface() {

        String millisTimeUrl = "http://192.168.1.17/time.php";

        @Override
        public long currentTimeMillis() throws IOException {
            return Long.parseLong(NetUtil.makeGetRequest(millisTimeUrl));
        }

        @Override
        public JSONObject getCurrentSeasonStateJson() throws IOException, JSONException {
            Log.d(TAG, "Getting current season state");
            String raw = Util.readLocalFile("data/current.json");
            return new JSONObject(raw);
        }

        @Override
        public JSONObject getFullSeasonSchedule(int year) throws IOException, JSONException {
            String raw = Util.readLocalFile(String.format("data/schedule-%d-full.json", year));
            return new JSONObject(raw);
        }
    };
}
