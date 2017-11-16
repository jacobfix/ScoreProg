package jacobfix.scorepredictor;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.Season;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class LobbyActivity extends AppCompatActivity {

    private static final String TAG = LobbyActivity.class.getSimpleName();

    private Season season;

    private Toolbar toolbar;
    private TextView toolbarTitle;

    private TabLayout tabs;
    private ViewPager pager;
    private LobbyPagerAdapter pagerAdapter;

    private AsyncCallback gameSyncCallback; // Atomic games!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating lobby");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby_new);

        /* LobbyActivity takes a year as an argument and retrieves the Season object corresponding
           to that year from Schedule. */
        int year = getIntent().getIntExtra("season", Schedule.getCurrentSeason());
        season = Schedule.getSeason(year);

        toolbar = ViewUtil.findById(this, R.id.toolbar);
        ViewUtil.initToolbar(this, toolbar);
        toolbar.setBackgroundColor(ColorUtil.STANDARD_COLOR);

        toolbarTitle = ViewUtil.findById(toolbar, R.id.title);
        toolbarTitle.setTypeface(FontHelper.getLobsterRegular(this));
        toolbarTitle.setAllCaps(false);
        toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        toolbarTitle.setText(R.string.app_name);

        // initializeToolbar();
        initializeViews();
        initializeTabs();
        initializeCallbacks();

        /* Have the current week's tab be selected by default. */
        pager.setCurrentItem(season.getAbsoluteWeekNumber(Schedule.getCurrentWeek(),
                Schedule.getCurrentSeasonType()));
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
            case R.id.action_find_friends:
                switchToFindUsersActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeToolbar() {
        toolbar = ViewUtil.findById(this, R.id.toolbar);
        // ViewUtil.initializeToolbar(this, toolbar);

        toolbar.setBackgroundColor(ColorUtil.STANDARD_COLOR);
        TextView toolbarTitle = ViewUtil.findById(this, R.id.title);
        // toolbarTitle.setTypeface(FontHelper.getYantramanavBold(this));
        toolbarTitle.setTypeface(FontHelper.getLobsterRegular(this));
        toolbarTitle.setAllCaps(false);
        toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
        // toolbarTitle.setText("Thursday, Sept. 14");
        toolbarTitle.setText("ScoreProg");

        /*
        toolbar.setBackgroundColor(Color.parseColor("#213642"));
        TextView title = ViewUtil.findById(this, R.id.title);
        title.setText("Lobby");
        toolbar.setPadding(0, ViewUtil.getStatusBarHeight(this), 0, 0);
        toolbar.getLayoutParams().height = toolbar.getLayoutParams().height + ViewUtil.getStatusBarHeight(this);
        toolbar.requestLayout();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        */
    }

    private void initializeViews() {
        tabs = ViewUtil.findById(this, R.id.tabs);
        pager = ViewUtil.findById(this, R.id.pager);
    }

    private void initializeTabs() {
        Log.d(TAG, "initializeTabs()");
        pagerAdapter = new LobbyPagerAdapter(getSupportFragmentManager(), season);
        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);
        // tabs.setSelectedTabIndicatorColor(Color.WHITE);
    }

    private void initializeCallbacks() {
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
        Season currentSeason = Schedule.getSeason(Schedule.getCurrentSeason());
        Season.Week currentWeek = currentSeason.getWeek(Schedule.getCurrentWeek(), Schedule.getCurrentSeasonType());

        LinkedList<AtomicGame> atomicGames = new LinkedList<>();
        for (String gameId : currentWeek.getGames()) {
            AtomicGame game = Schedule.getGame(gameId);
            Log.d(TAG, game.getAwayTeam().getAbbr() + " @ " + game.getHomeTeam().getAbbr());
            atomicGames.add(game);
        }
        */

        // GameProvider.setGamesToSync(atomicGames);
        // TODO: Don't register the sync callback here, do it in the fragment
        // GameProvider.registerSyncCallback(gameSyncCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void switchToFindUsersActivity() {
        Intent intent = new Intent(this, FindUsersActivity.class);
        startActivity(intent);
    }
}
