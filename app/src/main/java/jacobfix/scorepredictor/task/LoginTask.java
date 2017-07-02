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
import jacobfix.scorepredictor.UserSyncManager;
import jacobfix.scorepredictor.sync.SyncListener;
import jacobfix.scorepredictor.util.NetUtil;

public class LoginTask extends BaseTask<Integer> {

    private static final String TAG = LoginTask.class.getSimpleName();

    private String mUsernameEmail;
    private String mPassword;

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

    public LoginTask(String usernameEmail, String password, TaskFinishedListener listener) {
        super(listener);
        mUsernameEmail = usernameEmail;
        mPassword = password;
    }

    @Override
    public void execute() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(PARAM_USERNAME_EMAIL, mUsernameEmail);
        params.put(PARAM_PASSWORD, mPassword);
        params.put(PARAM_IS_EMAIL, String.valueOf(Patterns.EMAIL_ADDRESS.matcher(mUsernameEmail).matches()));

        try {
            String r = NetUtil.makePostRequest(LOGIN_URL, params);
            JSONObject response = new JSONObject(r);

            if (response.getBoolean("success")) {
                final String myId = response.getString("uid");
                UserSyncManager.getInstance().syncInForeground(myId, null, new SyncListener() {
                    @Override
                    public void onSyncFinished() {
                        Log.d(TAG, "Login user sync success");
                        UserSyncManager.getInstance().setMe(myId);
                        UserSyncManager.getInstance().setUsersOfBackgroundSync(Collections.singletonList(myId));
                        Log.d(TAG, UserSyncManager.getInstance().me().getFriends().toString());
                        for (String friendId : UserSyncManager.getInstance().me().getFriends())
                            UserSyncManager.getInstance().addUserToBackgroundSync(friendId);
                        mResult = LOGIN_ERROR_NONE;
                    }

                    @Override
                    public void onSyncError() {
                        /* Problem sync'ing me. Login not completed. */
                        Log.d(TAG, "Login user sync failed!");
                        mResult = LOGIN_ERROR_SYNC_FAILURE;
                    }
                });
            } else {
                mResult = Integer.valueOf(response.getString("errno"));
            }
        } catch (JSONException e) {
            reportError(e, TAG, TaskError.JSON_ERROR, e.toString());
            mResult = LOGIN_ERROR_UNKNOWN_JSON;
        } catch (IOException e) {
            reportError(e, TAG, TaskError.IO_ERROR, e.toString());
            mResult = LOGIN_ERROR_UNKNOWN_NETWORK;
        } catch (Exception e) {
            reportError(e, TAG, TaskError.UNKNOWN_ERROR, e.toString());
            mResult = LOGIN_ERROR_UNKNOWN;
        }
    }
}
