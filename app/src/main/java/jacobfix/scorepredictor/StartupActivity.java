package jacobfix.scorepredictor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jacobfix.scorepredictor.sync.NflGameOracle;
import jacobfix.scorepredictor.sync.SyncListener;
import jacobfix.scorepredictor.sync.UserOracle;

/**
 * Launcher activity. Checks if this is the user's first time launching the
 * app, or if the user is logged in, and passes this information on to
 * LobbyActivity. Displays a splash screen for the time it takes to check
 * user's login status and retrieve his profile information from the
 * server if he is indeed logged in.
 */
public class StartupActivity extends AppCompatActivity {

    private static final String TAG = StartupActivity.class.getSimpleName();

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

    private static final String PREF_NAME = "jacobfix.scorepredictor";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_LOGGED_IN = "login_status";
    private static final String KEY_USER_ID = "user_id";

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
        NflGameOracle.getInstance().registerSyncListener(mSyncListener);

        mPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        mEditor = mPreferences.edit();

        Intent intent = new Intent(this, LobbyActivity.class);

        // TODO: Instead of check for first launch, do check for version
        if (mPreferences.getBoolean(KEY_FIRST_LAUNCH, true)) {
            mEditor.putBoolean(KEY_FIRST_LAUNCH, false);
            intent.putExtra(EXTRA_FIRST_LAUNCH, true);
        }

        /* Because the above will be a version check, this login bit should run each time,
           even on the first launch, when it is guaranteed to return LOGIN_NEGATIVE. */
        // TODO: I guess it should just be the user ID stored on the device
        int loginStatus;
        if (mPreferences.getBoolean(KEY_LOGGED_IN, false)) {
            loginStatus = attemptLogin(mPreferences.getString(KEY_USER_ID, null));
            // TODO: Get the user ID and do
        } else {
            loginStatus = LOGIN_NEGATIVE;
        }
        intent.putExtra(EXTRA_LOGGED_IN, loginStatus);

        startActivity(intent);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        NflGameOracle.getInstance().unregisterSyncListener(mSyncListener);
    }

    // TODO: Just create a "me" for the time being
    private int attemptLogin(String userId) {
        /* Get username and password from device storage and make login request to server. */
        // TODO: Maybe userId and session key instead?
        if (userId == null) {
            return LOGIN_NEGATIVE;
        }
        // TODO: Have table with usernames, passwords, and all that and corresponding user ID
        // TODO: On login, return the information from that user ID
        // Have to consider the fact that provided user ID might not exist. How do we handle errors like that?
        UserOracle.getInstance().sync(userId, true);
        if (UserOracle.getInstance().userExists(userId)) {
            UserOracle.getInstance().setMe(userId);
        } else {
            return LOGIN_NEGATIVE;
        }

        mEditor.putBoolean(KEY_LOGGED_IN, true);
        return LOGIN_POSITIVE;
    }
}
