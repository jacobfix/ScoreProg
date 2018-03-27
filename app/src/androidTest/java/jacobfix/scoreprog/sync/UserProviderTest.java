package jacobfix.scoreprog.sync;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Map;

import jacobfix.scoreprog.AsyncCallback;
import jacobfix.scoreprog.users.UserDetails;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UserProviderTest {

    private static final String TAG = UserProviderTest.class.getSimpleName();

    @Test
    public void userProvider_getUserDetails() {
        ArrayList<String> userIds = new ArrayList<>();
        userIds.add("1");
        userIds.add("2");
        userIds.add("3");
        UserProvider.getUserDetails(userIds, new AsyncCallback<Map<String, UserDetails>>() {
            @Override
            public void onSuccess(Map<String, UserDetails> result) {
                assertTrue(result.size() == 3);
                assertNotNull(result.get("1"));
                assertNotNull(result.get("2"));
                assertNull(result.get("5"));
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue(false);
            }
        });
    }
}