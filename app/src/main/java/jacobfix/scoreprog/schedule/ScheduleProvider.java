package jacobfix.scoreprog.schedule;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ScheduleProvider {

    private static final String TAG = ScheduleProvider.class.getSimpleName();

    private static ScheduleProvider instance;

    private XmlPullParserFactory xmlFactory;

    private static final String SCHEDULE_URL = "http://www.nfl.com/ajax/scorestrip?season=%s&seasonType=%s&week=%d";

    public static synchronized ScheduleProvider get() {
        if (instance == null) {
            instance = new ScheduleProvider();
            try {
                instance.xmlFactory = XmlPullParserFactory.newInstance();
            } catch (XmlPullParserException e) {
                Log.e(TAG, e.toString());
            }
        }
        return instance;
    }

    public void getSchedule() {

    }

}
