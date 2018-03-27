package jacobfix.scoreprog.util;

import org.junit.Test;

import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

public class UtilTest {

    private static final String TAG = UtilTest.class.getSimpleName();

    @Test
    public void testTranslateMillisSinceEpochToTimeAndDate() {
        long millisSinceEpoch = 1390216980000L; /* 3:23 AM Jan. 20, 2014 */
        String timeAndDate = Util.translateMillisSinceEpochToLocalDateString(millisSinceEpoch, new SimpleDateFormat("h:mm a@M/d"));
        String[] split = timeAndDate.split("@");
        assertEquals("3:23 AM", split[0]);
        assertEquals("1/20", split[1]);
    }
}