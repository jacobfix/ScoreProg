package jacobfix.scorepredictor;

import android.os.Bundle;

import org.junit.Test;

import jacobfix.scorepredictor.schedule.ScheduleRetriever;

import static org.junit.Assert.*;

public class ScheduleRetrieverTest {
    @Test
    public void testGetCurrentSeasonState() throws Exception {
        Bundle bundle = ScheduleRetriever.get().getCurrentSeasonState();
        assertEquals(bundle.get("year"), 2017);
        assertEquals(bundle.get("week"), 0);
        assertEquals(bundle.get("type"), "PRE");
    }

}
