package jacobfix.scoreprog.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scoreprog.LocalAccountManager;
import jacobfix.scoreprog.util.NetUtil;

public abstract class UserServerInterface {

    private static final String TAG = UserServerInterface.class.getSimpleName();

    public static final int FRIENDS_ALL = 0;
    public static final int FRIENDS_CONFIRMED = 1;
    public static final int FRIENDS_PENDING = 2;

    public static UserServerInterface getDefault() {
        return traditional;
    }

    public abstract JSONArray getUserDetails(Collection<String> userIds) throws Exception;

    public abstract boolean validateToken(String userId, String token) throws Exception;
    public abstract JSONObject login(String usernameEmail, String password, boolean isEmail) throws Exception;
    public abstract JSONObject register(String username, String email, String password) throws Exception;

    public abstract JSONArray getFriends(int which) throws Exception;
    public abstract JSONArray getMatchingUsernames(String usernamePrefix) throws Exception;

    public abstract boolean requestFriend(String userId) throws Exception;
    public abstract boolean confirmFriendRequest(String userId) throws Exception;
    public abstract boolean cancelFriendRequest(String userId) throws Exception;

    private static UserServerInterface traditional = new UserServerInterface() {

        final String BASE_URL = "http://jsoftworks.com/scoreprog/";

        final String GET_USER_DETAILS_URL = BASE_URL.concat("get_users.php");

        final String VALIDATE_URL = BASE_URL.concat("validate.php");
        final String LOGIN_URL = BASE_URL.concat("login.php");
        final String REGISTER_URL = BASE_URL.concat("register.php");

        final String GET_FRIENDS_URL = BASE_URL.concat("get_friends.php");
        final String MATCHING_USERNAMES_URL = BASE_URL.concat("find_users.php");
        final String REQUEST_FRIEND_URL = BASE_URL.concat("request_friend.php");
        final String CONFIRM_FRIEND_URL = BASE_URL.concat("confirm_friend.php");
        final String CANCEL_FRIEND_URL = BASE_URL.concat("cancel_friend.php");

        @Override
        public JSONArray getUserDetails(Collection<String> userIds) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            params.put("user_id", userIds.toArray(new String[userIds.size()]));

            JSONObject response = new JSONObject(NetUtil.makeGetRequest(GET_USER_DETAILS_URL, headers, params));
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return response.getJSONArray("payload");
        }

        @Override
        public boolean validateToken(String userId, String token) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, userId, token);

            String s = NetUtil.makeGetRequest(VALIDATE_URL, headers, null);
            Log.d(TAG, "Validate response: " + s);
            JSONObject response = new JSONObject(s);
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return true;
        }

        @Override
        public JSONObject login(String usernameEmail, String password, boolean isEmail) throws Exception {
            Map<String, String[]> params = new HashMap<>();
            if (isEmail) params.put("email", new String[]{usernameEmail});
            else         params.put("username", new String[]{usernameEmail});
            params.put("password", new String[]{password});

            String s = NetUtil.makePostRequest(LOGIN_URL, null, params);
            JSONObject response = new JSONObject(s);
            // JSONObject response = new JSONObject(NetUtil.makePostRequest(LOGIN_URL, null, params));
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return response.getJSONObject("payload");
        }

        @Override
        public JSONObject register(String username, String email, String password) throws Exception {
            Map<String, String[]> params = new HashMap<>();
            params.put("username", new String[]{username});
            params.put("email", new String[]{email});
            params.put("password", new String[]{password});

            String s = NetUtil.makePostRequest(REGISTER_URL, null, params);
            Log.d(TAG, s);
            JSONObject response = new JSONObject(s);
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return response.getJSONObject("payload");
        }

        @Override
        public JSONArray getFriends(int which) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            String[] whichParam = new String[1];
            switch (which) {
                case FRIENDS_ALL:
                    whichParam[0] = "all";
                    break;

                case FRIENDS_CONFIRMED:
                    whichParam[0] = "confirmed";
                    break;

                case FRIENDS_PENDING:
                    whichParam[0] = "pending";
                    break;
            }
            params.put("which", whichParam);

            JSONObject response = new JSONObject(NetUtil.makeGetRequest(GET_FRIENDS_URL, headers, params));
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return response.getJSONArray("payload");
        }

        @Override
        public JSONArray getMatchingUsernames(String usernamePrefix) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            params.put("prefix", new String[]{usernamePrefix});

            String s = NetUtil.makeGetRequest(MATCHING_USERNAMES_URL, headers, params);
            Log.d(TAG, s);
            JSONObject response = new JSONObject(s);
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return response.getJSONArray("payload");
        }

        @Override
        public boolean requestFriend(String userId) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            params.put("user_id", new String[]{userId});

            JSONObject response = new JSONObject(NetUtil.makePostRequest(REQUEST_FRIEND_URL, headers, params));
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return true;
        }

        @Override
        public boolean confirmFriendRequest(String userId) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            params.put("user_id", new String[]{userId});

            JSONObject response = new JSONObject(NetUtil.makePostRequest(CONFIRM_FRIEND_URL, headers, params));
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return true;
        }

        @Override
        public boolean cancelFriendRequest(String userId) throws Exception {
            Map<String, String> headers = new HashMap<>();
            NetUtil.addAuthHeader(headers, LocalAccountManager.get().userId, LocalAccountManager.get().token);

            Map<String, String[]> params = new HashMap<>();
            params.put("user_id", new String[]{userId});

            JSONObject response = new JSONObject(NetUtil.makePostRequest(CANCEL_FRIEND_URL, headers, params));
            if (!response.getBoolean("success"))
                throw new ServerException(response.getInt("error"));

            return true;
        }
    };
}
