package jacobfix.scorepredictor.schedule;

import android.os.Bundle;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.TimeZone;

import jacobfix.scorepredictor.util.NetUtil;

public class ScheduleRetriever {

    private static final String TAG = ScheduleRetriever.class.getSimpleName();

    private static ScheduleRetriever instance;

    private static XmlPullParserFactory factory;
    private static XmlPullParser parser;

    private static SimpleDateFormat dateFormat;

    private static String CURRENT_WEEK_URL = "http://www.nfl.com/liveupdate/scorestrip/ss.xml";
    private static String WEEK_URL = "http://www.nfl.com/ajax/scorestrip?season=%d&seasonType=%s&week=%d";

    private static String DATE_FORMAT_STRING = "yyyyMMdd HH:mm aa";
    private static String TIME_ZONE = "US/Eastern";

    private static final int MAX_PREGAME_WEEKS = 10;

    public static synchronized ScheduleRetriever get() {
        if (instance == null)
            instance = new ScheduleRetriever();
        return instance;
    }

    public ScheduleRetriever() {
        try {
            factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();

            dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
            dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        } catch (XmlPullParserException e) {

        }
    }

    public Bundle getCurrentSeasonState() throws XmlPullParserException, IOException {
        /* Returns a Bundle containing the following keys:
            "year"
            "week"
            "type" */
        String xml = NetUtil.makeGetRequest(CURRENT_WEEK_URL);
        parser.setInput(new StringReader(xml));

        int t = parser.getEventType();
        while (t != XmlPullParser.END_DOCUMENT) {
            if (t == XmlPullParser.START_TAG && parser.getName().equals("gms")) {
                Bundle b = new Bundle();
                int season = Integer.parseInt(parser.getAttributeValue(null, "y"));
                int week = Integer.parseInt(parser.getAttributeValue(null, "w"));
                int seasonType = parseSeasonType(parser.getAttributeValue(null, "t"), week);

                b.putInt("year", season);
                b.putInt("week", week);
                b.putInt("type", seasonType);
                return b;
            } else {
                t = parser.next();
            }
        }
        return null;
    }

    /*
    public JSONObject getWeek(int season, int week, int type) throws JSONException, IOException, XmlPullParserException {
        String seasonType = null;
        switch (type) {
            case Schedule.PRE:
                seasonType = "PRE";
                break;
            case Schedule.REG:
                seasonType = "REG";
                break;
            case Schedule.POS:
                seasonType = "POS";
                break;
        }

        String url = String.format(WEEK_URL, season, week, seasonType);
        String xml = NetUtil.makeGetRequest(url);
        parser.setInput(new StringReader(xml));

        JSONObject root = new JSONObject();
        JSONObject games = new JSONObject();

        int t = parser.getEventType();
        while (t != XmlPullParser.END_DOCUMENT) {
            if (t == XmlPullParser.START_TAG) {
                switch (parser.getName()) {
                    case "gms":
                        root.put("season", parser.getAttributeValue(null, "y"));
                        root.put("week", parser.getAttributeValue(null, "w"));
                        root.put("seasonType", parser.getAttributeValue(null, "t"));
                        break;
                    case "g":
                        JSONObject game = new JSONObject();
                        String gameId = parser.getAttributeValue(null, "eid");
                        game.put("id", gameId);
                        game.put("day", parser.getAttributeValue(null, "d"));
                        game.put("time", parser.getAttributeValue(null, "t"));
                        game.put("home", parser.getAttributeValue(null, "h"));
                        game.put("away", parser.getAttributeValue(null, "v"));
                        game.put("type", parser.getAttributeValue(null, "gt"));
                        games.put(gameId, game);
                        break;
                }
            }
            t = parser.next();
        }
        root.put("games", games);
        return root;
    }
    */

    public ScheduledGame[] getWeek(int season, int week, int type) throws Exception {
        String seasonType = null;
        switch (type) {
            case Schedule.PRE:
                seasonType = "PRE";
                break;
            case Schedule.REG:
                seasonType = "REG";
                break;
            case Schedule.POS:
                seasonType = "POS";
                break;
        }

        String url = String.format(WEEK_URL, season, seasonType, week);
        String xml = NetUtil.makeGetRequest(url);
        parser.setInput(new StringReader(xml));

        int currentSeason = -1;
        int currentWeek = -1;
        int currentSeasonType = -1;

        LinkedList<ScheduledGame> scheduledGames = new LinkedList<>();

        int t = parser.getEventType();
        while (t != XmlPullParser.END_DOCUMENT) {
            if (t == XmlPullParser.START_TAG) {
                switch (parser.getName()) {
                    case "gms":
                        currentSeason = Integer.parseInt(parser.getAttributeValue(null, "y"));
                        currentWeek = Integer.parseInt(parser.getAttributeValue(null, "w"));
                        currentSeasonType = parseSeasonType(parser.getAttributeValue(null, "t"), currentWeek);
                        break;
                    case "g":
                        ScheduledGame scheduledGame = new ScheduledGame();
                        scheduledGame.gid = parser.getAttributeValue(null, "eid");
                        scheduledGame.awayAbbr = parser.getAttributeValue(null, "v");
                        scheduledGame.homeAbbr = parser.getAttributeValue(null, "h");

                        // TODO: Start time display has to be converted to local time zone
                        String meridiem = "pm";
                        scheduledGame.meridiem = meridiem;
                        String startTimeString = parser.getAttributeValue(null, "t");
                        scheduledGame.startTimeDisplay = startTimeString;
                        String dateString = scheduledGame.gid.substring(0, 8) + " " + startTimeString + " " + meridiem;
                        Log.d(TAG, "Date string: " + dateString + " " + dateFormat.getTimeZone().getDisplayName());
                        scheduledGame.startTime = dateFormat.parse(dateString).getTime();
                        Log.d(TAG, "Time since epoch: " + scheduledGame.startTime);

                        scheduledGame.dayOfWeek = parseDayOfWeek(parser.getAttributeValue(null, "d"));
                        scheduledGame.season = currentSeason;
                        scheduledGame.week = currentWeek;
                        scheduledGame.seasonType = currentSeasonType;
                        scheduledGame.weekType = parseWeekType(parser.getAttributeValue(null, "gt"), currentWeek);

                        scheduledGames.add(scheduledGame);
                        break;
                }
            }
            t = parser.next();
        }
        Log.d(TAG, "Scheduled Games size: " + scheduledGames.size());
        return scheduledGames.toArray(new ScheduledGame[scheduledGames.size()]);
    }

    private Schedule.Day parseDayOfWeek(String day) {
        switch (day) {
            case "SUN":
                return Schedule.Day.SUN;
            case "MON":
                return Schedule.Day.MON;
            case "TUE":
                return Schedule.Day.TUE;
            case "WED":
                return Schedule.Day.WED;
            case "THU":
                return Schedule.Day.THU;
            case "FRI":
                return Schedule.Day.FRI;
            case "SAT":
                return Schedule.Day.SAT;
        }
        return null;
    }

    private int parseSeasonType(String type, int week) {
        if (type.equals("P")) {
            if (week > MAX_PREGAME_WEEKS) return Schedule.POS;
            else                          return Schedule.PRE;
        } else {
            return Schedule.REG;
        }
    }

    private Season.WeekType parseWeekType(String type, int week) {
        switch(type) {
            case "PRE":
                if (week == 0) return Season.WeekType.HOF;
                return Season.WeekType.PRE;
            case "REG":
                return Season.WeekType.REG;
            case "WC":
                return Season.WeekType.WC;
            case "DIV":
                return Season.WeekType.DIV;
            case "CON":
                return Season.WeekType.CON;
            case "SB":
                return Season.WeekType.SB;
        }
        return null;
    }
}
