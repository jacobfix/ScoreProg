package jacobfix.scoreprog;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import jacobfix.scoreprog.users.User;

public class UserJsonParser {

    private static final String TAG = UserJsonParser.class.getSimpleName();

    private static UserJsonParser instance;

    public static synchronized UserJsonParser getInstance() {
        if (instance == null)
            instance = new UserJsonParser();
        return instance;
    }

    public ArrayList<String> updateUserInfo(User[] users, JSONObject infoJson) throws JSONException {
        ArrayList<String> updated = new ArrayList<>();
        /* Iterate over the JSON. */
        Iterator<String> iterator = infoJson.keys();
        while (iterator.hasNext()) {
            String userId = iterator.next();
            JSONObject userJson = infoJson.getJSONObject(userId);

            /* Linear scan for now. */
            User user = linearScan(users, userId);

            Log.d(TAG, "About to enter synchronized block");
            synchronized (user) {
                user.setUsername(userJson.getString("username"));
                user.setEmail(userJson.getString("email"));

                HashSet<String> friendIds = new HashSet<>();
                JSONArray friendIdsJson = userJson.getJSONArray("friends");
                for (int i = 0; i < friendIdsJson.length(); i++) {
                    friendIds.add(friendIdsJson.getString(i));
                }
                user.setFriends(friendIds);
            }

            updated.add(userId);
        }
        return updated;
    }

    public ArrayList<String> updateUserPredictions(User[] users, JSONObject predictionsJson) throws JSONException {
        ArrayList<String> updated = new ArrayList<>();
        Iterator<String> iterator = predictionsJson.keys();
        while (iterator.hasNext()) {
            String gameId = iterator.next();
            Log.d(TAG, "Game ID: " + gameId);
            JSONObject gameJson = predictionsJson.getJSONObject(gameId);

            Iterator<String> innerIterator = gameJson.keys();
            while (innerIterator.hasNext()) {
                String userId = innerIterator.next();
                JSONObject userJson = gameJson.getJSONObject(userId);

                Log.d(TAG, "User ID: " + userId);
                User user = linearScan(users, userId);

                synchronized (user) {
                    Prediction p = new Prediction(gameId, userJson.getInt("away"), userJson.getInt("home"));
                    user.getPredictions().put(gameId, p);
                }
            }
            updated.add(gameId);
        }
        return updated;
    }

    private User linearScan(User[] users, String userId) {
        for (int i = 0; i < users.length; i++) {
            if (users[i].getId().equals(userId))
                return users[i];
        }
        return null;
    }
}
