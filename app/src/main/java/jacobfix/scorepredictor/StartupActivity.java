package jacobfix.scorepredictor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jacobfix.scorepredictor.friends.UserOracle;

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

    private static final String PREF_NAME = "jacobfix.scorepredictor";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_LOGGED_IN = "login_status";

    public static final String EXTRA_FIRST_LAUNCH = "first_launch";
    public static final String EXTRA_LOGGED_IN = "logged_in";

    public static final int LOGIN_POSITIVE = 1;
    public static final int LOGIN_NEGATIVE = 2;
    public static final int LOGIN_ERROR = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        mEditor = mPreferences.edit();

        Intent intent = new Intent(this, LobbyActivity.class);

        // TODO: Instead of check for first launch, do check for version
        if (mPreferences.getBoolean(KEY_FIRST_LAUNCH, true)) {
            mEditor.putBoolean(KEY_FIRST_LAUNCH, false);
            intent.putExtra(EXTRA_FIRST_LAUNCH, true);
        } else {
            int loginStatus;
            if (mPreferences.getBoolean(KEY_LOGGED_IN, false)) {
                loginStatus = login();
            } else {
                loginStatus = LOGIN_NEGATIVE;
            }
            intent.putExtra(EXTRA_LOGGED_IN, loginStatus);
        }

        startActivity(intent);
        finish();
    }

    private int login() {
        /* Get some value from local memory that we can use to make a request to the server for
           all of this user's information. */
        // UserOracle.getInstance().sync();
        // Maybe would rather have it be the task itself, that way we can run it in the foreground
        // In that case, we'd have to have a separate statement that adds us to the UsersToSync
        mEditor.putBoolean(KEY_LOGGED_IN, true);
        return LOGIN_POSITIVE;
    }
}
