package jacobfix.scorepredictor.task;

import android.util.Log;
import android.util.Patterns;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.ApplicationContext;
import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.LocalAccountManager;
import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.util.NetUtil;

public class LoginTask extends BaseTask {

    private static final String TAG = LoginTask.class.getSimpleName();

    private String usernameEmail;
    private String password;

    private static final String LOGIN_URL = "http://" + ApplicationContext.HOST + "/login.php";

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
    public static final int LOGIN_ERROR_UNKNOWN = 9;

    public LoginTask(String ue, String p, TaskFinishedListener listener) {
        super(listener);
        usernameEmail = ue;
        password = p;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_USERNAME_EMAIL, usernameEmail);
        params.put(PARAM_PASSWORD, password);
        params.put(PARAM_IS_EMAIL, String.valueOf(Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches()));

        try {
            JSONObject response = new JSONObject(NetUtil.makePostRequest(LOGIN_URL, params));

            if (response.getBoolean("success")) {
                final String loggedInId = response.getString("uid");
                UserProvider.getUserDetails(Collections.singletonList(loggedInId), new AsyncCallback<Map<String, User>>() {
                    @Override
                    public void onSuccess(Map<String, User> result) {
                        Log.d(TAG, "Got details after login successfully");
                        User thisUser = (User) result.keySet().toArray()[0];
                        LocalAccountManager.set(thisUser);
                        UserProvider.setDetailsToSync(LocalAccountManager.getFriends());
                        setResult(LOGIN_ERROR_NONE);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Getting user details after login failed");
                        setResult(LOGIN_ERROR_SYNC_FAILURE);
                    }
                });

            }
        } catch (IOException e) {
            reportError(e, TAG, TaskError.JSON_ERROR, e.toString());
            setResult(LOGIN_ERROR_UNKNOWN_NETWORK);
        } catch (JSONException e) {
            reportError(e, TAG, TaskError.IO_ERROR, e.toString());
            setResult(LOGIN_ERROR_UNKNOWN_JSON);
        } catch (Exception e) {
            reportError(e, TAG, TaskError.UNKNOWN_ERROR, e.toString());
            setResult(LOGIN_ERROR_UNKNOWN);
        }
    }
}
