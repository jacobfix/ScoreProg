package jacobfix.scorepredictor.task;

import android.util.Log;
import android.util.Patterns;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.ApplicationContext;
import jacobfix.scorepredictor.AsyncCallback;
import jacobfix.scorepredictor.LocalAccountManager;
import jacobfix.scorepredictor.server.UserServerInterface;
import jacobfix.scorepredictor.sync.OriginalUserProvider;
import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.users.UserDetails;
import jacobfix.scorepredictor.util.NetUtil;

public class LoginTask extends BaseTask<Integer> {

    private static final String TAG = LoginTask.class.getSimpleName();

    private String usernameEmail;
    private String password;

    private static final String LOGIN_URL = "http://" + ApplicationContext.HOST + "/login.php";

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
        try {
            boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches();
            JSONObject response = UserServerInterface.getDefault().authenticate(usernameEmail, password, isEmail);

            if (response.getBoolean("success")) {
                final String loggedInId = response.getString("uid");
                Log.d(TAG, "Got ID: " + loggedInId);
                UserProvider.getUserDetails(Collections.singletonList(loggedInId), new AsyncCallback<Map<String, UserDetails>>() {
                    @Override
                    public void onSuccess(Map<String, UserDetails> result) {
                        Log.d(TAG, "Got UserDetails of logged in user");
                        UserDetails userDetails = result.get(loggedInId);
                        LocalAccountManager.get().init(userDetails);
                        setResult(LOGIN_ERROR_NONE);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        reportError(e);
                    }
                }, true);
            } else {
                setResult(response.getInt("errno"));
            }
        } catch (Exception e) {
            reportError(e);
        }
    }
}
