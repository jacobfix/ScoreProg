package jacobfix.scorepredictor.task;

import android.util.Patterns;

import org.json.JSONObject;

import jacobfix.scorepredictor.server.ServerErrorCode;
import jacobfix.scorepredictor.server.UserServerInterface;

public class AuthenticateTask extends BaseTask<AuthenticateTask.AuthenticationResult> {

    private String usernameEmail;
    private String password;

    public AuthenticateTask(String usernameEmail, String password, TaskFinishedListener listener) {
        super(listener);
        this.usernameEmail = usernameEmail;
        this.password = password;
    }

    @Override
    public void execute() {
        try {
            AuthenticationResult result = new AuthenticationResult();

            boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches();
            JSONObject response = UserServerInterface.getDefault().authenticate(usernameEmail, password, isEmail);

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

    public class AuthenticationResult {

        private String userId;
        private boolean authenticated;
        private ServerErrorCode error;

        public boolean authenticated() {
            return authenticated;
        }

        public String getUserId() {
            return userId;
        }

        public void success(String uid) {
            authenticated = true;
            userId = uid;
        }

        public void failure(ServerErrorCode code) {
            authenticated = false;
            error = code;
        }

        public ServerErrorCode getError() {
            return error;
        }
    }
}
