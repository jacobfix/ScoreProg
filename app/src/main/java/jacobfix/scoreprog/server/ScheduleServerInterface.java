package jacobfix.scoreprog.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jacobfix.scoreprog.schedule.Season;
import jacobfix.scoreprog.util.NetUtil;
import jacobfix.scoreprog.util.Util;

public abstract class ScheduleServerInterface {

    private static final String TAG = ScheduleServerInterface.class.getSimpleName();

    public static ScheduleServerInterface getDefault() {
        return traditional;
    }

    public abstract long currentTimeMillis() throws IOException;
    public abstract JSONObject getCurrentSeasonStateJson() throws IOException, JSONException;
    public abstract JSONObject getFullSeasonSchedule(int year) throws IOException, JSONException;
    
    protected abstract String getBaseUrl();
    protected abstract String getCurrentSeasonStateUrl();
    protected abstract String getFullSeasonScheduleUrl();

    public abstract void updateSeason(Season season, JSONObject json) throws JSONException;

    private static TraditionalScheduleServerInterface traditional = new TraditionalScheduleServerInterface();
    private static MockScheduleServerInterface mock = new MockScheduleServerInterface();

    private static class TraditionalScheduleServerInterface extends ScheduleServerInterface {

        protected String currentTimeUrl = "http://jsoftworks.com/scoreprog/time.php";
        
        @Override
        protected String getBaseUrl() {
            return "http://jsoftworks.com/scoreprog/schedule/";
        }
        
        @Override
        protected String getCurrentSeasonStateUrl() {
            return getBaseUrl().concat("current.json");
        }
        
        @Override
        protected String getFullSeasonScheduleUrl() {
            return getBaseUrl().concat("%d/full.json");
        }
        
        @Override
        public long currentTimeMillis() throws IOException {
            return Long.parseLong(NetUtil.makeGetRequest(currentTimeUrl, null, null));
        }

        @Override
        public JSONObject getCurrentSeasonStateJson() throws IOException, JSONException {
            return new JSONObject(NetUtil.makeGetRequest(getCurrentSeasonStateUrl(), null, null));
        }

        @Override
        public JSONObject getFullSeasonSchedule(int year) throws IOException, JSONException {
            return new JSONObject(NetUtil.makeGetRequest(String.format(getFullSeasonScheduleUrl(), year), null, null));
        }

        @Override
        public void updateSeason(Season season, JSONObject json) throws JSONException {
            synchronized (season.acquireLock()) {
                updateSeasonSegment(season.getPreseason(), json.getJSONArray("PRE"));
                updateSeasonSegment(season.getRegularSeason(), json.getJSONArray("REG"));
                updateSeasonSegment(season.getPostseason(), json.getJSONArray("POST"));
            }
        }

        public void updateSeasonSegment(Season.SeasonSegment segment, JSONArray json) throws JSONException {
            // synchronized (segment.acquireLock()) {
            for (int i = 0; i < json.length(); i++) {
                JSONObject weekJson = json.getJSONObject(i);

                int weekNumber = weekJson.getInt("week_number");
                Season.WeekType weekType = Util.parseWeekType(weekJson.getString("week_type"), weekNumber);

                JSONArray gamesJson = weekJson.getJSONArray("games");
                String[] weekGames = new String[gamesJson.length()];

                for (int j = 0; j < gamesJson.length(); j++) {
                    JSONObject gameJson = gamesJson.getJSONObject(i);

                }
            }
            // }
        }
    }

    private static class MockScheduleServerInterface extends TraditionalScheduleServerInterface {

        @Override
        protected String getBaseUrl() {
            return "http://jsoftworks.com/scoreprog/mock/schedule/";
        }
    }

//    private static ScheduleServerInterface old = new ScheduleServerInterface() {
//
//        String currentSeasonStateUrl = "http://thefixhome.com/sp/schedule/current";
//        String fullSeasonScheduleUrl = "http://thefixhome.com/sp/schedule/%d/full";
//
//        String millisTimeUrl = "http://thefixhome.com/sp/time.php";
//
//        @Override
//        public long currentTimeMillis() throws IOException {
//            return Long.parseLong(NetUtil.makeGetRequest(millisTimeUrl, null, null));
//        }
//
//        @Override
//        public JSONObject getCurrentSeasonStateJson() throws IOException, JSONException {
//            Log.d(TAG, "Getting current season state");
//            String response = NetUtil.makeGetRequest(currentSeasonStateUrl, null, null);
//            return new JSONObject(response);
//        }
//
//        @Override
//        public JSONObject getFullSeasonSchedule(int year) throws IOException, JSONException {
//            String response = NetUtil.makeGetRequest(String.format(fullSeasonScheduleUrl, year), null, null);
//            return new JSONObject(response);
//        }
//
//        @Override
//        public void updateSeason(Season season, JSONObject json) throws JSONException {
//
//        }
//    };
}
