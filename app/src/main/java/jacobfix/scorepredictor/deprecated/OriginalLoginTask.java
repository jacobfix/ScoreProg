package jacobfix.scorepredictor.deprecated;

import android.util.Log;
import android.util.Patterns;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.ApplicationContext;
import jacobfix.scorepredictor.server.JsonParser;
import jacobfix.scorepredictor.sync.UserOracle;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.util.NetUtil;

public class OriginalLoginTask extends BaseTask {

    private static final String TAG = OriginalLoginTask.class.getSimpleName();

    private String mUsernameEmail;
    private String mPassword;

    // private static final String LOGIN_URL = "http://192.168.1.15/login.php";
    // private static final String LOGIN_URL = "http://172.20.8.156/login.php";
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

    public OriginalLoginTask(String usernameEmail, String password, TaskFinishedListener listener) {
        super(listener);
        mUsernameEmail = usernameEmail;
        mPassword = password;
    }

    @Override
    public void execute() {
        Log.d(TAG, "Executing OriginalLoginTask");
        Map<String, String> params = new HashMap<String, String>();
        params.put(PARAM_USERNAME_EMAIL, mUsernameEmail);
        params.put(PARAM_PASSWORD, mPassword);
        params.put(PARAM_IS_EMAIL, String.valueOf(Patterns.EMAIL_ADDRESS.matcher(mUsernameEmail).matches()));

        try {
            String r = NetUtil.makePostRequest(LOGIN_URL, params);
            JSONObject response = new JSONObject(r);
            Log.d(TAG, "GOT RESPONSE: " + response.toString());
            if (response.getBoolean("success")) {
                /* User ID is given in response. We then call sync() on that. */
                String myId = JsonParser.parseLoginSuccess(response);

                Log.d(TAG, "About to sync user");
                /* Sync on this thread. */
                /* UserSyncManager.getInstance().sync(myId, new SyncListener() {
                    @Override
                    public void onSyncFinished() {

                    }

                    @Override
                    public void onSyncError() {

                    }
                }); */

                UserOracle.getInstance().sync(myId, true); // TODO: Consider where else this can be used
                Log.d(TAG, "Just sync'd user");

                if (UserOracle.getInstance().syncErrorOccurred()) {
                    mResult = LOGIN_ERROR_SYNC_FAILURE;
                    return;
                }
                Log.d(TAG, "All users: " + UserOracle.getInstance().mUsers);
                if (UserOracle.getInstance().mUsers.isEmpty()) {
                    Log.e(TAG, "mUsers is empty when it should contain \"me\"!");
                }
                UserOracle.getInstance().setMe(myId);
                Log.d(TAG, "Set me: " + UserOracle.getInstance().me());

                mResult = LOGIN_ERROR_NONE;
            } else {
                int errno = JsonParser.parseLoginFailure(response);
                mResult = errno;
            }
        } catch (IOException e) {
            reportError(e, TAG, TaskError.IO_ERROR, e.toString());
            mResult = LOGIN_ERROR_UNKNOWN_NETWORK;
        } catch (JSONException e) {
            reportError(e, TAG, TaskError.JSON_ERROR, e.toString());
            mResult = LOGIN_ERROR_UNKNOWN_JSON;
        } catch (Exception e) {
            reportError(e, TAG, TaskError.UNKNOWN_ERROR, e.toString());
        }
    }
}
