package jacobfix.scoreprog;

import android.animation.ArgbEvaluator;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.sync.GameProvider;
import jacobfix.scoreprog.sync.PredictionProvider;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.Util;
import jacobfix.scoreprog.util.ViewUtil;

import static jacobfix.scoreprog.ApplicationContext.ACTION_ANNOUNCE_GAME_LOCKED;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = GameActivity.class.getSimpleName();

    private String[] gameIds;

    private Toolbar toolbar;
    private TextView toolbarTitle;
    private TabLayout tabs;
    private ViewPager pager;
    private GameActivityPagerAdapter pagerAdapter;

    private Map<String, Prediction> predictions;

    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private BroadcastReceiver gameLockedBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "GameActivity onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_new);

        gameIds = getIntent().getStringArrayExtra("gameIds");
        final int currentIndex = getIntent().getIntExtra("current", 0);
        String weekName = getIntent().getStringExtra("title");

        toolbar = ViewUtil.findById(this, R.id.toolbar);
        ViewUtil.initToolbar(this, toolbar, weekName, FontHelper.getYantramanavBold(this), 18, 0);
        ((TextView) ViewUtil.findById(toolbar, R.id.title)).setAllCaps(true);

        pagerAdapter = new GameActivityPagerAdapter(getSupportFragmentManager(), gameIds);

        tabs = ViewUtil.findById(this, R.id.game_tabs);
        pager = ViewUtil.findById(this, R.id.pager);

        pager.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pager);

        PredictionProvider.getMyPredictions(Arrays.asList(gameIds), new AsyncCallback<Map<String, Prediction>>() {
            @Override
            public void onSuccess(Map<String, Prediction> result) {
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

        gameLockedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String gameId = intent.getStringExtra("gameId");

                SparseArray<GameFragment> allGameFragments = pagerAdapter.getRegisteredFragments();
                for (int i = 0; i < allGameFragments.size(); i++)
                    allGameFragments.valueAt(i).onGameLocked(gameId);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(gameLockedBroadcastReceiver, new IntentFilter(ACTION_ANNOUNCE_GAME_LOCKED));

        pager.setCurrentItem(currentIndex);
    }

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
//            case R.id.action_set_exposure:
//                Log.d(TAG, "Set exposure clicked");
//                return true;
            case R.id.view_friends:
                switchToFriendsActivity();
                return true;

            case R.id.find_users:
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

    @Override
    public void onStop() {
        super.onStop();
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

    private void switchToFriendsActivity() {
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
    }

    private void switchToFindUsersActivity() {
        Intent intent = new Intent(this, FindUsersActivity.class);
        startActivity(intent);
    }
}
