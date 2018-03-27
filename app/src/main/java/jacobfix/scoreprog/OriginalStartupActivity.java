package jacobfix.scoreprog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Collections;
import java.util.Map;

import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.sync.SyncListener;
import jacobfix.scoreprog.sync.UserProvider;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.users.UserDetails;
// import jacobfix.scoreprog.sync.UserOracle;

/**
 * Launcher activity. Checks if this is the user's first time launching the
 * app, or if the user is logged in, and passes this information on to
 * OriginalLobbyActivity. Displays a splash screen for the time it takes to check
 * user's login status and retrieve his profile information from the
 * server if he is indeed logged in.
 */
public class OriginalStartupActivity extends AppCompatActivity {

    private static final String TAG = OriginalStartupActivity.class.getSimpleName();

    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;

    SyncListener mSyncListener = new SyncListener() {
        @Override
        public void onSyncFinished() {
            /* Do nothing. */
        }

        @Override
        public void onSyncError() {

        }
    };

    private static final String PREF_NAME = "jacobfix.scoreprog";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_LOGGED_IN = "login_status";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN = "token";

    public static final String EXTRA_FIRST_LAUNCH = "first_launch";
    public static final String EXTRA_LOGGED_IN = "logged_in";

    public static final int LOGIN_POSITIVE = 1;
    public static final int LOGIN_NEGATIVE = 2;
    public static final int LOGIN_ERROR = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Immediately begin the first sync of the NflGameOracle. */
        // NflGameOracle.getInstance().sync();

        /* This is a bit of a hack. We just want to kick off the scheduled sync, which is achieved
           by registering a sync listener, but a sync listener is unneeded for this activity, so we
           just pass in one that does nothing. This is exploiting the fact that the next activity's
           onStart() method is called before this activity's onStop() method, so the next sync
           listener will be registered before this one is unregistered, thereby keeping the scheduled
           sync running across the transition. */
        // NflGameOracle.getInstance().registerSyncListener(mSyncListener);

        mPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        mEditor = mPreferences.edit();

        final Intent intent = new Intent(this, LobbyActivity.class);

        // TODO: Instead of check for first launch, do check for version
        if (mPreferences.getBoolean(KEY_FIRST_LAUNCH, true)) {
            mEditor.putBoolean(KEY_FIRST_LAUNCH, false);
            intent.putExtra(EXTRA_FIRST_LAUNCH, true);
        }

        TaskFinishedListener afterSeasonLoaded = new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    /* Initialization error occurred. */
                    Log.e(TAG, "" + task.getError());
                    return;
                }
                boolean loggedIn = (boolean) task.getResult();
                if (loggedIn) switchToLobbyActivity();
                else          switchToLoginActivity();
            }
        };

        Log.d(TAG, "Starting task to load schedule");
        new BaseTask<Boolean>(afterSeasonLoaded) {
            @Override
            public void execute() {
                try {
                    LockGameManager.get().init();
                    Schedule.init();

                    final String authToken = mPreferences.getString(KEY_TOKEN, null);
                    if (authToken != null) {

                    } else {

                    }

                    boolean loggedIn = mPreferences.getBoolean(KEY_LOGGED_IN, false);
                    if (loggedIn) {
                        final String loggedInId = mPreferences.getString(KEY_USER_ID, null);
                        UserProvider.getUserDetails(Collections.singletonList(loggedInId),
                                new AsyncCallback<Map<String, UserDetails>>() {
                            @Override
                            public void onSuccess(Map<String, UserDetails> result) {
                                UserDetails userDetails = result.get(loggedInId);
                                // LocalAccountManager.get().init(userDetails);
                                setResult(true);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                reportError(e);
                            }
                        });
                    } else {
                        setResult(false);
                    }
                } catch (Exception e) {
                    reportError(e);
                    e.printStackTrace();
                    // TODO: Display a dialog fragment announcing that there was an error loading the season
                    Log.e(TAG, "There was an error loading the season: " + e);
                    setResult(false);
                }
            }
        }.start();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void switchToLobbyActivity() {
        Intent intent = new Intent(this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }

    private void switchToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
