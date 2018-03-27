package jacobfix.scoreprog;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

import jacobfix.scoreprog.util.Util;

@RunWith(AndroidJUnit4.class)
public class JsonUpdateTest {

    private static final String TAG = JsonUpdateTest.class.getSimpleName();

    @Test
    public void testUpdatePlayFromJson() throws Exception {
        Play play = new Play();

        String filename1 = "test/test-play-update-1.json";
        String filename2 = "test/test-play-update-2.json";

        JSONObject json1 = new JSONObject(Util.readLocalFile(filename1));
        play.updateFromJson(json1);

        assertEquals(3, play.getDown());
        assertEquals("NYJ 40", play.getYardLine());
        assertEquals(3, play.getQuarter());
        assertEquals("05:57", play.getClock());
        assertEquals(2, play.getYardsToGo());
        assertEquals(17, play.getYardsNet());
        assertEquals(2, play.numSequenceItems());

        JSONObject json2 = new JSONObject(Util.readLocalFile(filename2));
        play.updateFromJson(json2);

        assertEquals(4, play.getDown());
        assertEquals("NYJ 40", play.getYardLine());
        assertEquals(3, play.getQuarter());
        assertEquals("03:00", play.getClock());
        assertEquals(1, play.getYardsToGo());
        assertEquals(14, play.getYardsNet());
        assertEquals(3, play.numSequenceItems());
    }

    @Test
    public void testUpdateDriveFromJson() throws Exception {
        Drive drive = new Drive();

        String filename1 = "test/test-drive-update-1.json";

        JSONObject json1 = new JSONObject(Util.readLocalFile(filename1));
        drive.updateFromJson(json1);

        assertEquals("NYJ", drive.getPosTeam());
        assertEquals(2, drive.getQuarter());
        assertEquals(true, drive.inRedZone());
        assertEquals("Field Goal", drive.getResult());
        assertEquals("0:31", drive.getTimeOfPossession());
        assertEquals(32, drive.getYardsGained());
        assertEquals(2, drive.getStartQuarter());
        assertEquals(2, drive.getEndQuarter());
        assertEquals("00:31", drive.getStartClock());
        assertEquals("00:00", drive.getEndClock());
        assertEquals("NYJ 29", drive.getStartYardLine());
        assertEquals("CLE 39", drive.getEndYardLine());
        assertEquals("NYJ", drive.getStartTeam());
        assertEquals("NYJ", drive.getEndTeam());

        assertEquals(8, drive.numPlays());
        ArrayList<Play> plays = drive.plays();
        Play firstPlay = plays.get(0);
        assertEquals("00:31", firstPlay.getClock());
        assertEquals("NYJ", firstPlay.getPosTeam());
        assertEquals(1, firstPlay.getDown());
        assertEquals(2, firstPlay.getQuarter());
        assertEquals(11, firstPlay.getYardsNet());
        assertEquals("NYJ 29", firstPlay.getYardLine());
    }

    @Test
    public void testUpdateDriveFeedFromJson() throws Exception {
        DriveFeed driveFeed = new DriveFeed();

        String filename = "test/test-drive-feed-update-1.json";

        JSONObject json = new JSONObject(Util.readLocalFile(filename));
        driveFeed.updateFromJson(json);

        assertEquals(23, driveFeed.numDrives());

        Drive firstDrive = driveFeed.getDrive(0);

        assertEquals(1, firstDrive.getQuarter());
        assertEquals(true, firstDrive.inRedZone());
        assertEquals("Interception", firstDrive.getResult());

    }
}
