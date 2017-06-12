package jacobfix.scorepredictor;

import android.util.Patterns;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.server.JsonParser;
import jacobfix.scorepredictor.sync.UserOracle;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.util.NetUtil;

public class Login {

    private static final String TAG = Login.class.getSimpleName();

    private static final String LOGIN_URL = "http://192.168.1.15/login";

    private static final String PARAM_USERNAME_EMAIL = "username_email";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_IS_EMAIL = "is_email";

    public static final int LOGIN_ERROR_NONE = 0;
    public static final int LOGIN_ERROR_INSUFFICIENT_PARAMS = 1;
    public static final int LOGIN_ERROR_EMAIL_NO_MATCH = 2;
    public static final int LOGIN_ERROR_USERNAME_NO_MATCH = 3;
    public static final int LOGIN_ERROR_INVALID_PASSWORD = 4;
    public static final int LOGIN_ERROR_DATABASE_FAILURE = 5;
    public static final int LOGIN_ERROR_UNKNOWN_NETWORK = 6;
    public static final int LOGIN_ERROR_UNKNOWN_JSON = 7;
    public static final int LOGIN_ERROR_SYNC_FAILURE = 8;

    // TODO: This should return some kind of status integer
    public static int login(String usernameEmail, String password) {
        final Map<String, String> params = new HashMap<>();
        params.put(PARAM_USERNAME_EMAIL, usernameEmail);
        params.put(PARAM_PASSWORD, password);
        params.put(PARAM_IS_EMAIL, String.valueOf(Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches()));

        try {
            // TODO: Can't do networking operation on main thread; must make a task to do this
            TaskFinishedListener listener = new TaskFinishedListener() {
                @Override
                public void onTaskFinished(BaseTask task) {
                    try {
                        JSONObject response = (JSONObject) task.getResult();
                        if (response.getBoolean("success")) {
                            // Could just return user ID and then call sync on that
                            // User me = JsonParser.parseLoginSuccess(response);
                            // UserOracle.getInstance().setMe(me);
                            String myId = JsonParser.parseLoginSuccess(response);
                            UserOracle.getInstance().sync(myId, true);
                            UserOracle.getInstance().setMe(myId);
                            // return LOGIN_ERROR_NONE;
                        } else {
                            int errno = JsonParser.parseLoginFailure(response);
                            // return errno;
                        }
                    } catch (JSONException e) {

                    }
                }
            };
            BaseTask task = new BaseTask() {
                @Override
                public void execute() {
                    try {
                        mResult = NetUtil.makePostRequest(LOGIN_URL, params);
                    } catch (IOException e) {
                        reportError(e, TAG, TaskError.IO_ERROR, e.toString());
                    } catch (JSONException e) {
                        reportError(e, TAG, TaskError.JSON_ERROR, e.toString());
                    }
                }
            };
            task.start();
            String response = NetUtil.makePostRequest(LOGIN_URL, params);
            /* Guaranteed to have "success" key. */

        } catch (IOException e) {
            return LOGIN_ERROR_UNKNOWN_NETWORK;
        } catch (JSONException e) {
            return LOGIN_ERROR_UNKNOWN_JSON;
        }
        return 0;
    }

    public enum Result {
        SUCCESS,
        INCORRECT_PASSWORD,
        EMAIL_NO_MATCH,
        USERNAME_NO_MATCH,
        UNKNOWN_ERROR,
    }
}
