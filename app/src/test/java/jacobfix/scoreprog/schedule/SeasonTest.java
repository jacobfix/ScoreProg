package jacobfix.scoreprog.schedule;

import android.content.Context;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import jacobfix.scoreprog.util.Util;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SeasonTest {

    @Mock
    Context context;

    @Test
    public void populateTest() throws Exception {
        int year = 2017;

        Season season = new Season(year);
        // JSONObject json = ScheduleServerInterface.getDefault().getFullSeasonSchedule(year);
        JSONObject json = new JSONObject(Util.readLocalFile(context, "data/schedule-2017-full.json"));
        season.populate(json);

        assertEquals(season.getNumberOfWeeks(Season.SeasonType.POST), 0);
    }
}