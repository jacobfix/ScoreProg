package jacobfix.scoreprog;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

import jacobfix.scoreprog.server.JsonParser;
import jacobfix.scoreprog.server.ServerException;
import jacobfix.scoreprog.server.UserServerInterface;
import jacobfix.scoreprog.sync.GameProvider;
import jacobfix.scoreprog.sync.PredictionProvider;
import jacobfix.scoreprog.sync.UserProvider;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.GetFriendsTask;
import jacobfix.scoreprog.task.SyncFriendsTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.util.Wrapper;

public class Login {

    private static final String TAG = Login.class.getSimpleName();

    private static final String PREF_NAME = "scoreprog";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN = "token";

    public static Pair<String, String> getLocalCredentials(Context context) {
        /* Retrieves the locally stored user ID and token. The presence of these two values indicate
           an already logged in user. */
        String userId = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_USER_ID, null);
        String token = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_TOKEN, null);
        return new Pair<String, String>(userId, token);
    }

    public static void setLocalCredentials(Context context, String userId, String token) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public static boolean validateCredentials(String userId, String token) throws Exception {
        if (userId == null || token == null)
            return false;
        return UserServerInterface.getDefault().validateToken(userId, token);
    }

    public static LoginResult login(String usernameEmail, String password, boolean isEmail) throws Exception {
        JSONObject json;
        try {
            json = UserServerInterface.getDefault().login(usernameEmail, password, isEmail);
        } catch (ServerException e) {
            return new LoginResult(e);
        }
        return JsonParser.createLoginResult(json);
    }

    public static LoginResult register(String username, String email, String password) throws Exception {
        JSONObject json;
        try {
            json = UserServerInterface.getDefault().register(username, email, password);
        } catch (ServerException e) {
            return new LoginResult(e);
        }
        return JsonParser.createLoginResult(json);
    }

    public static void logout() {
        LocalAccountManager.get().clearUserInfo();
        setLocalCredentials(ApplicationContext.getContext(), null, null);
        UserProvider.clearUserDetailsCache();
        PredictionProvider.clearPredictionCache();
        PredictionProvider.clearParticipantsCache();
    }

    public static void setupLocalAccount(String userId, String token) throws Exception {
        LocalAccountManager.get().setUser(userId, token);

        final Wrapper<Exception> possibleException = new Wrapper<>();

        new SyncFriendsTask(LocalAccountManager.get().friends, new TaskFinishedListener<SyncFriendsTask>() {
            @Override
            public void onTaskFinished(SyncFriendsTask task) {
                if (task.errorOccurred()) {
                    possibleException.set(task.getError());
                    return;
                }
            }
        }).startOnThisThread();

        if (possibleException.isPresent())
            throw possibleException.get();
    }

    public static class LoginResult {
        String token;
        String userId;

        Exception error;

        public LoginResult(String userId, String token) {
            this.token = token;
            this.userId = userId;
        }

        public LoginResult(Exception e) {
            this.error = e;
        }

        public boolean successful() {
            return error == null;
        }
    }
}
