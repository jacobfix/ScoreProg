package jacobfix.scorepredictor;

import android.animation.ArgbEvaluator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;

import jacobfix.scorepredictor.components.MyToolbar;
import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.sync.PredictionProvider;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = GameActivity.class.getSimpleName();

    private String[] gameIds;

    private Toolbar toolbar;
    private TextView toolbarTitle;
    private TabLayout tabs;
    private ViewPager pager;
    private GameActivityPagerAdapter pagerAdapter;

    private Predictions predictions;

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private BroadcastReceiver gameLockBroadcastReceiver;

    public static final String ACTION_ANNOUNCE_GAME_LOCKED = "lock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_new);

        gameIds = getIntent().getStringArrayExtra("gameIds");
        final int currentIndex = getIntent().getIntExtra("current", 0);
        String weekName = getIntent().getStringExtra("title");

        toolbar = ViewUtil.findById(this, R.id.toolbar);
        ViewUtil.initToolbar(this, toolbar);

        toolbarTitle = ViewUtil.findById(toolbar, R.id.title);
        toolbarTitle.setTypeface(FontHelper.getYantramanavBold(this));
        toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        toolbarTitle.setText(weekName);

        pagerAdapter = new GameActivityPagerAdapter(getSupportFragmentManager(), gameIds);

        tabs = ViewUtil.findById(this, R.id.game_tabs);
        pager = ViewUtil.findById(this, R.id.pager);

        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);

        PredictionProvider.getPredictions(Collections.singletonList(LocalAccountManager.get().getId()), Arrays.asList(gameIds), false, new AsyncCallback<Predictions>() {
            @Override
            public void onSuccess(Predictions result) {
                predictions = result;

                pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        Log.d(TAG, "Position: " + position + ", Position Offset: " + positionOffset);
                        GameFragment currentFragment = pagerAdapter.getRegisteredFragment(position);
                        GameFragment nextFragment = pagerAdapter.getRegisteredFragment(position + 1);

                        int startColor = getFragmentBaseColor(currentFragment);
                        int endColor = getFragmentBaseColor(nextFragment);

                        int color = (int) argbEvaluator.evaluate(positionOffset, startColor, endColor);
                        // setStatusBarColor(color);
                        setToolbarColor(color);

                        // if (currentFragment != null) currentFragment.setScoreboardColor(color);
                        // if (nextFragment != null)    nextFragment.setScoreboardColor(color);
                    }

                    @Override
                    public void onPageSelected(int position) {

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });


        gameLockBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String gameId = intent.getStringExtra("gameId");
                for (String gid : gameIds) {
                    if (gid.equals(gid)) {

                    }
                }
            }
        };


        pager.setCurrentItem(currentIndex);
    }

//    private void initializeToolbar() {
//        // toolbar = ViewUtil.findById(this, R.id.toolbar);
//        // ViewUtil.initializeToolbar(this, toolbar);
//
//        // toolbar.setBackgroundColor(ColorUtil.STANDARD_COLOR);
//        TextView title = ViewUtil.findById(this, R.id.title);
//        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//        title.setTypeface(FontHelper.getYantramanavBold(this));
//        title.setText(getIntent().getStringExtra("title"));
//    }

    /*
    private void initializeToolbar() {
        toolbar = ViewUtil.findById(this, R.id.toolbar);
        toolbar.setPadding(0, ViewUtil.getStatusBarHeight(this), 0, 0);
        toolbar.getLayoutParams().height = toolbar.getLayoutParams().height + ViewUtil.getStatusBarHeight(this);
        toolbar.requestLayout();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
    */

//    private void initializeTabs() {
//        tabs = ViewUtil.findById(this, R.id.game_tabs);
//        pager = ViewUtil.findById(this, R.id.pager);
//
//        pager.setAdapter(pagerAdapter);
//
//        tabs.setupWithViewPager(pager);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_set_exposure:
                Log.d(TAG, "Set exposure clicked");
                return true;
            case R.id.action_find_friends:
                switchToFindUsersActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private synchronized void changeScoreboardColor(GameFragment fragment, int color) {
        if (fragment == pagerAdapter.getRegisteredFragment(pager.getCurrentItem())) {
            Util.changeStatusBarColor(this, color);
            toolbar.setBackgroundColor(color);
            tabs.setBackgroundColor(color);
            fragment.setScoreboardColor(color);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setStatusBarColor(int color) {
        Util.changeStatusBarColor(this, color);
    }

    private void setToolbarColor(int color) {
        toolbar.setBackgroundColor(color);
        tabs.setBackgroundColor(color);
    }

    public void setStatusBarColor(int color, GameFragment fragment) {
        if (fragment == pagerAdapter.getRegisteredFragment(pager.getCurrentItem()))
            Util.changeStatusBarColor(this, color);
    }

    public void setToolbarColor(int color, GameFragment fragment) {
        if (fragment == pagerAdapter.getRegisteredFragment(pager.getCurrentItem())) {
            toolbar.setBackgroundColor(color);
            tabs.setBackgroundColor(color);
        }
    }

    private int getFragmentBaseColor(GameFragment fragment) {
        if (fragment == null)
            return ColorUtil.STANDARD_TEXT;

        return fragment.getScoreboardColor();
        /*
        Prediction fragmentPrediction = fragment.getPrediction();
        if (fragmentPrediction == null)
            return ColorUtil.STANDARD_TEXT;

        // TODO: synchronized prediction
        switch (fragmentPrediction.winner()) {
            case Prediction.W_AWAY:
                return Schedule.getGame(fragmentPrediction.getGameId()).getAwayColor();

            case Prediction.W_HOME:
                return Schedule.getGame(fragmentPrediction.getGameId()).getHomeColor();

            default:
                return ColorUtil.STANDARD_TEXT;
        }
        */
    }

    private void switchToFindUsersActivity() {
        Intent intent = new Intent(this, FindUsersActivity.class);
        startActivity(intent);
    }
}
