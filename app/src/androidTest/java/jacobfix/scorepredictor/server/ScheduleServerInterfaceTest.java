package jacobfix.scorepredictor.server;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ScheduleServerInterfaceTest {

    private final int currentWeek = 1;
    private final String currentType = "PRE";

    @Test
    public void getCurrent() throws Exception {
        JSONObject json = ScheduleServerInterface.getDefault().getCurrentSeasonStateJson();
        assertNotNull(json);
        assertEquals(json.getInt("week"), currentWeek);
        assertEquals(json.getString("type"), "PRE");
    }

}