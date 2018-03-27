package jacobfix.scoreprog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Arrays;

import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.schedule.Season;
import jacobfix.scoreprog.sync.GameProvider;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

import static jacobfix.scoreprog.ApplicationContext.ACTION_ANNOUNCE_GAME_LOCKED;

public class LobbyActivity extends AppCompatActivity {

    private static final String TAG = LobbyActivity.class.getSimpleName();

    private Season season;

    private Toolbar toolbar;
    private TextView toolbarTitle;

    private TabLayout tabs;
    private ViewPager pager;
    private LobbyPagerAdapter pagerAdapter;

    private BroadcastReceiver gameLockedBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_new);

        /* LobbyActivity takes a year as an argument and retrieves the Season object corresponding
           to that year from Schedule. */
        int year = getIntent().getIntExtra("season", Schedule.getCurrentYear());
        season = Schedule.getSeason(year);

        toolbar = ViewUtil.findById(this, R.id.toolbar);
        ViewUtil.initToolbar(this, toolbar, R.string.app_name, FontHelper.getLobsterRegular(this), 26, ColorUtil.STANDARD_COLOR);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initializeViews();
        initializeTabs();

        /* Have the current week's tab be selected by default. */
        pager.setCurrentItem(season.getAbsoluteWeekNumber(Schedule.getCurrentWeekNumber(), Schedule.getCurrentSeasonType()));

        gameLockedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String gameId = intent.getStringExtra("gameId");

                SparseArray<LobbyFragment> allLobbyFragments = pagerAdapter.getRegisteredFragments();
                for (int i = 0; i < allLobbyFragments.size(); i++)
                    allLobbyFragments.valueAt(i).onGameLocked(gameId);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(gameLockedBroadcastReceiver, new IntentFilter(ACTION_ANNOUNCE_GAME_LOCKED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_lobby, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_friends:
                switchToFriendsActivity();
                return true;

            case R.id.sign_out:
                Login.logout();
                switchToLoginActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        tabs = ViewUtil.findById(this, R.id.tabs);
        pager = ViewUtil.findById(this, R.id.pager);
    }

    private void initializeTabs() {
        pagerAdapter = new LobbyPagerAdapter(getSupportFragmentManager(), season);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
    }

    @Override
    public void onStart() {
        super.onStart();
        /* Every time the LobbyActivity is started, we set the GameProvider's games to sync to be the
           games of the current week in the season. */
//        Season.Week currentWeek = Schedule.getCurrentWeek();
//        GameProvider.setGamesToSync(currentWeek.getGames());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void switchToFriendsActivity() {
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
    }

    private void switchToFindUsersActivity() {
        Intent intent = new Intent(this, FindUsersActivity.class);
        startActivity(intent);
    }

    private void switchToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
