package jacobfix.scorepredictor.server;

import android.util.Patterns;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.util.NetUtil;
import jacobfix.scorepredictor.util.Util;

public abstract class UserServerInterface {

    public static UserServerInterface getDefault() {
        return traditional;
    }

    /*
    { "uid_1": {"username": ..., "email": ..., "friends": ..., "medals": ...},
      "uid_2": {...},
    }
     */
    public abstract JSONObject authenticate(String usernameEmail, String password, boolean isEmail) throws IOException, JSONException;
    public abstract JSONObject register(String username, String email, String password) throws IOException, JSONException;
    public abstract JSONObject getUserDetailsJson(Collection<String> userIds) throws IOException, JSONException;
    public abstract JSONObject findMatchingUsernames(String username) throws IOException, JSONException;
    public abstract JSONObject getFriendsJson(String userId) throws IOException, JSONException;

    private static UserServerInterface traditional = new UserServerInterface() {

        private static final String PARAM_USERNAME_EMAIL = "username_email";
        private static final String PARAM_PASSWORD = "password";
        private static final String PARAM_IS_EMAIL = "is_email";

        private static final String LOGIN_URL = "http://thefixhome.com/sp/login.php";
        private static final String REGISTER_URL = "http://thefixhome.com/sp/register.php";
        private static final String GET_USERS_URL = "http://thefixhome.com/sp/users/get_users.php";
        private static final String FIND_USERS_URL = "http://thefixhome.com/sp/users/find_users.php";
        private static final String GET_FRIENDS_URL = "http://thefixhome.com/sp/users/get_friends.php";

        private static final String LOGIN_KEY = "";
        private static final String REGISTER_KEY = "";

        @Override
        public JSONObject authenticate(String usernameEmail, String password, boolean isEmail) throws IOException, JSONException {
            Map<String, String> params = new HashMap<>();
            params.put(PARAM_USERNAME_EMAIL, usernameEmail);
            params.put(PARAM_PASSWORD, password);
            params.put(PARAM_IS_EMAIL, String.valueOf(Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches()));
            return new JSONObject(NetUtil.makePostRequest(LOGIN_URL, params));
        }

        @Override
        public JSONObject register(String username, String email, String password) throws IOException, JSONException {
            Map<String, String> params = new HashMap<>();
            params.put("username", username);
            params.put("email", email);
            params.put("password", password);
            return new JSONObject(NetUtil.makePostRequest(REGISTER_URL, params));
        }

        @Override
        public JSONObject getUserDetailsJson(Collection<String> userIds) throws IOException, JSONException {
            Map<String, String[]> params = new HashMap<>();
            params.put("uid", userIds.toArray(new String[userIds.size()]));
            return new JSONObject(NetUtil.makeGetRequest(GET_USERS_URL, params));
        }

        @Override
        public JSONObject findMatchingUsernames(String username) throws IOException, JSONException {
            Map<String, String[]> params = new HashMap<>();
            params.put("username", new String[]{username});
            return new JSONObject(NetUtil.makeGetRequest(FIND_USERS_URL, params));
        }

        public JSONObject findMatchingUsernames(String username, int limit) throws IOException, JSONException {
            Map<String, String[]> params = new HashMap<>();
            params.put("username", new String[]{username});
            params.put("limit", new String[]{String.valueOf(limit)});
            return new JSONObject(NetUtil.makeGetRequest(FIND_USERS_URL, params));
        }

        @Override
        public JSONObject getFriendsJson(String userId) throws IOException, JSONException {
            Map<String, String[]> params = new HashMap<>();
            params.put("uid", new String[]{userId});
            return new JSONObject(NetUtil.makeGetRequest(GET_FRIENDS_URL, params));
        }
    };

    private static UserServerInterface test = new UserServerInterface() {
        @Override
        public JSONObject authenticate(String usernameEmail, String password, boolean isEmail) throws JSONException {
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("uid", "1");
            return json;
        }

        @Override
        public JSONObject register(String username, String email, String password) throws JSONException {
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("uid", "1");
            return json;
        }

        @Override
        public JSONObject getUserDetailsJson(Collection<String> userIds) throws JSONException {
            JSONObject result = new JSONObject();
            for (String userId : userIds) {
                result.put(userId, new JSONObject(String.format("{\"username\": %s, \"email\": %s}", Util.randomString(10), Util.randomString(10))));
            }
            return result;
        }

        @Override
        public JSONObject findMatchingUsernames(String username) {
            return null;
        }

        @Override
        public JSONObject getFriendsJson(String userId) {
            return null;
        }
    };
}
