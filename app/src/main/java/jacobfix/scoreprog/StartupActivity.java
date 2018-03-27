package jacobfix.scoreprog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;

import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.TaskFinishedListener;

public class StartupActivity extends AppCompatActivity {

    private static final String TAG = StartupActivity.class.getSimpleName();

    private StartupTask startupTask;

    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAILURE = 1;
    private static final int LOGIN_ERROR   = 2;
    private static final int LOADING_ERROR = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "About to run StartupTask");
        startupTask = new StartupTask(new TaskFinishedListener<StartupTask>() {
            @Override
            public void onTaskFinished(StartupTask task) {
                if (task.errorOccurred()) {
                    Log.d(TAG, "An error occurred");
                    Log.e(TAG, task.getError().toString());
                    displayErrorDialog();
                    return;
                }

                int resultCode = task.getResult();
                Log.d(TAG, "Startup result code: " + resultCode);
                switch(resultCode) {
                    case LOGIN_SUCCESS:
                        switchToLobbyActivity();
                        break;

                    case LOGIN_FAILURE:
                        switchToLoginActivity();
                        break;

                    case LOGIN_ERROR:
                        displayErrorDialog();
                        break;

                    case LOADING_ERROR:
                        displayErrorDialog();
                        break;

                    default:
                        displayErrorDialog();
                }
            }
        });
        startupTask.start();
    }

    private void displayErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("An error occurred during setup. Please ensure you are connected to the Internet.")
                .setCancelable(false)
                .setNeutralButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startupTask.start();
                    }
                });
        builder.create().show();
    }

    private void switchToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToLobbyActivity() {
        Intent intent = new Intent(this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }

    class StartupTask extends BaseTask<Integer> {

        public StartupTask(TaskFinishedListener listener) {
            super(listener);
        }

        @Override
        public void execute() {
            Log.d(TAG, "EXECUTING STARTUP TASK");
            try {
                LockGameManager.get().init();
                Log.d(TAG, "INITIALIZED LOCK GAME MANAGER");
                Schedule.init();
                Log.d(TAG, "INITIALIZED SCHEDULE");
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                setResult(LOADING_ERROR);
                return;
            }

            try {
                Pair<String, String> userIdAndToken = Login.getLocalCredentials(StartupActivity.this);
                Log.d(TAG, "UserID and Token: " + userIdAndToken.first + ", " + userIdAndToken.second);
                String userId = userIdAndToken.first;
                String token = userIdAndToken.second;
                if (Login.validateCredentials(userId, token)) {
                    Login.setupLocalAccount(userId, token);
                    setResult(LOGIN_SUCCESS);
                } else {
                    setResult(LOGIN_FAILURE);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                setResult(LOGIN_ERROR);
            }
        }
    }
}
