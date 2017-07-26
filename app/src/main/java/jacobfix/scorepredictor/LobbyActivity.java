package jacobfix.scorepredictor;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.util.ViewUtil;

public class LobbyActivity extends AppCompatActivity {

    private static final String TAG = LobbyActivity.class.getSimpleName();

    private DialogFragment loginDialog;

    private AsyncCallback weekSyncListener;

    private Toolbar toolbar;
    private TabLayout tabs;
    private ViewPager pager;
    private LobbyPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating lobby");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_new);

        Intent intent = getIntent();
        if (intent.getBooleanExtra(StartupActivity.EXTRA_FIRST_LAUNCH, false)) {
            /* Show first launch dialog. */
            Log.d(TAG, "FIRST LAUNCH/NEW VERSION DIALOG DISPLAYED");
            showLoginDialog();
        } else if (!intent.getBooleanExtra(StartupActivity.EXTRA_LOGGED_IN, false)) {
            /* Show login dialog. */
            showLoginDialog();
            Log.d(TAG, "LOGIN DIALOG DISPLAYED");
        } else {
            /* Procedure for logged in user. */
            Log.d(TAG, "USER IS LOGGED IN");
        }

        initializeToolbar();
        initializeViews();
        initializeTabs();
        initializeListeners();
    }

    private void initializeToolbar() {
        toolbar = ViewUtil.findById(this, R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#213642"));
    }

    private void initializeViews() {
        tabs = ViewUtil.findById(this, R.id.tabs);
        pager = ViewUtil.findById(this, R.id.pager);
    }

    private void initializeTabs() {
        Log.d(TAG, "initializeTabs()");
        Log.d(TAG, "" + Schedule.getCurrentSeasonType());
        // pagerAdapter = new LobbyPagerAdapter(getSupportFragmentManager(), Schedule.getSeason(Schedule.getCurrentSeason()));
        pagerAdapter = new LobbyPagerAdapter(getSupportFragmentManager(), Schedule.getSeason(Schedule.getCurrentSeason()));
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
        tabs.setSelectedTabIndicatorColor(Color.WHITE);
        // pager.setCurrentItem();
    }

    private void initializeListeners() {
        weekSyncListener = new AsyncCallback<Void>()  {
            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(Void v) {

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Schedule.registerWeekSyncListener(weekSyncListener);
    }

    private void showLoginDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        loginDialog = LoginDialogFragment.newInstance();
        loginDialog.show(ft, "login_dialog");
    }

    private void hideLoginDialog() {
        if (loginDialog != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(loginDialog);
        }
    }
}
