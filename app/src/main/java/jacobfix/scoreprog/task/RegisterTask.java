package jacobfix.scoreprog.task;

import android.util.Log;

import org.json.JSONObject;

import jacobfix.scoreprog.server.ServerErrorCode;
public class RegisterTask extends BaseTask<RegisterTask.RegistrationResult> {

    private static final String TAG = RegisterTask.class.getSimpleName();

    private String username;
    private String email;
    private String password;

    public RegisterTask(String username, String email, String password, TaskFinishedListener listener) {
        super(listener);
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void execute() {
        try {
            RegistrationResult result = new RegistrationResult();
            // JSONObject response = OriginalUserServerInterface.getDefault().register(username, email, password);

            JSONObject response = null;
            Log.d(TAG, response.toString());
            if (response.getBoolean("success")) {
                result.success(response.getString("uid"));
            } else {
                result.failure(ServerErrorCode.values()[response.getInt("errno")]);
            }
            setResult(result);
        } catch (Exception e) {
            reportError(e);
        }
    }

    public class RegistrationResult {

        private String userId;
        private boolean registered;
        private ServerErrorCode error;

        public boolean registered() {
            return registered;
        }

        public void success(String uid) {
            registered = true;
            userId = uid;
        }

        public void failure(ServerErrorCode code) {
            registered = false;
            error = code;
        }

        public String getUserId() {
            return userId;
        }

        public ServerErrorCode getError() {
            return error;
        }
    }
}
