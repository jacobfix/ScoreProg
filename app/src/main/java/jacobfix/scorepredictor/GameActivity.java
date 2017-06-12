package jacobfix.scorepredictor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import jacobfix.scorepredictor.components.FlipCardView;
import jacobfix.scorepredictor.sync.NflGameOracle;
import jacobfix.scorepredictor.sync.SyncListener;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.RankUsersTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.sync.UserOracle;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class GameActivity extends AppCompatActivity implements GameProvider {

    private static final String TAG = GameActivity.class.getSimpleName();

    private NflGame mGame;
    private ArrayList<User> mRankedParticipants;

    private Toolbar mToolbar;

    private LinearLayout mMiddleContainer;
    private LinearLayout mAwayNameContainer;
    private LinearLayout mHomeNameContainer;
    private FrameLayout mScoreboardContainer;
    private TextView mQuarter;
    private TextView mClock;
    
    private TextView mAwayAbbr;
    private TextView mHomeAbbr;
    private TextView mAwayName;
    private TextView mHomeName;
    private TextView mAwayScore;
    private TextView mHomeScore;
    
    private FlipCardView mAwayFlipCard;
    private FlipCardView mHomeFlipCard;

    private FlipCardView mSelectedFlipCard;

    private TabLayout mTabs;
    private ViewPager mPager;
    private GamePagerAdapter mPagerAdapter;

    private SyncListener mNflGameOracleSyncListener;
    private SyncListener mUserOracleSyncListener;

    private FriendPredictionFragment mFriendPredictionFragment;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final String GAME_ID_EXTRA = "game_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_material_new_new);

        /* This activity must be passed a game ID on creation. */
        String gameId = getIntent().getStringExtra(GAME_ID_EXTRA);
        if (gameId == null) Log.e(TAG, GameActivity.class.getSimpleName() + " was started without a game ID");
        mGame = NflGameOracle.getInstance().getActiveGame(gameId);
        if (mGame == null && (mGame = NflGameOracle.getInstance().getArchivedGame(gameId)) == null) {
            /* There's a problem. */
        }

        initializeActionBar();
        initializeViews();
        initializeTabsAndPager();
        initializeListeners();
        updateState();
        changeScoreboardColor(mGame.getAwayTeam().getPrimaryColor());
    }

    private void initializeActionBar() {
        mToolbar = ViewUtil.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initializeViews() {
        // mScoreboard = ViewUtil.findById(this, R.id.scoreboard);
        mMiddleContainer = ViewUtil.findById(this, R.id.middle_container);
        mAwayNameContainer = ViewUtil.findById(this, R.id.away_name_container);
        mHomeNameContainer = ViewUtil.findById(this, R.id.home_name_container);

        mScoreboardContainer = ViewUtil.findById(this, R.id.scoreboard_background);

        mQuarter = ViewUtil.findById(this, R.id.quarter);
        mClock = ViewUtil.findById(this, R.id.clock);

        mAwayAbbr = ViewUtil.findById(this, R.id.away_abbr);
        mHomeAbbr = ViewUtil.findById(this, R.id.home_abbr);
        mAwayName = ViewUtil.findById(this, R.id.away_name);
        mHomeName = ViewUtil.findById(this, R.id.home_name);

        mAwayAbbr.setTypeface(FontHelper.getYantramanavBold(this));
        mAwayName.setTypeface(FontHelper.getYantramanavBold(this));
        mHomeAbbr.setTypeface(FontHelper.getYantramanavBold(this));
        mHomeName.setTypeface(FontHelper.getYantramanavBold(this));

        ViewUtil.applyDeboss(mAwayAbbr);
        ViewUtil.applyDeboss(mAwayName);
        ViewUtil.applyDeboss(mHomeAbbr);
        ViewUtil.applyDeboss(mHomeName);

        mAwayScore = ViewUtil.findById(this, R.id.away_score_actual);
        mHomeScore = ViewUtil.findById(this, R.id.home_score_actual);
        
        mAwayScore.setTypeface(FontHelper.getYantramanavRegular(this));
        mHomeScore.setTypeface(FontHelper.getYantramanavRegular(this));

        ViewUtil.applyDeboss(mAwayScore);
        ViewUtil.applyDeboss(mHomeScore);

        mAwayFlipCard = ViewUtil.findById(this, R.id.away_flip_card);
        mHomeFlipCard = ViewUtil.findById(this, R.id.home_flip_card);

        View.OnClickListener onFlipCardClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mGame.isPregame()) {
                    return;
                }

                if (mSelectedFlipCard != null) {
                    /* If there is not an already selected flip card... */
                    if (mSelectedFlipCard == view) {
                        return;
                    }
                    confirmPrediction();
                }
                mSelectedFlipCard = (FlipCardView) view;
            }
        };
    }

    private void initializeTabsAndPager() {
        mPager = ViewUtil.findById(this, R.id.pager);
        mPagerAdapter = new GamePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mTabs = ViewUtil.findById(this, R.id.tabs);
        mTabs.setupWithViewPager(mPager);
        mTabs.setSelectedTabIndicatorColor(Color.WHITE);
    }

    private void initializeListeners() {
        mNflGameOracleSyncListener = new SyncListener() {
            @Override
            public void onSyncFinished() {
                NflGame updatedGame = NflGameOracle.getInstance().getActiveGame(mGame.getGameId());
                /* Might no longer be a part of the active games. */
                if (updatedGame == null) {
                    updatedGame = NflGameOracle.getInstance().getArchivedGame(mGame.getGameId());
                }
                mGame = updatedGame;
                // mScoreboard.updateState(mGame);
                updateState();
            }

            @Override
            public void onSyncError() {

            }
        };
        /* After the UserOracle syncs, we need to update the ranking of friends for this game. */
        mUserOracleSyncListener = new SyncListener() {
            @Override
            public void onSyncFinished() {
                Collection<User> friends = UserOracle.getInstance().getParticipatingFriends(mGame.getGameId());
                friends.add(UserOracle.getInstance().me());
                new RankUsersTask(friends, mGame.getGameId(), new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        mRankedParticipants = (ArrayList<User>) task.getResult();
                        mFriendPredictionFragment.setFriends(mRankedParticipants);
                    }
                });
            }

            @Override
            public void onSyncError() {

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NflGameOracle.getInstance().registerSyncListener(mNflGameOracleSyncListener);
        // UserOracle.getInstance().registerSyncListener(mUserOracleSyncListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        NflGameOracle.getInstance().unregisterSyncListener(mNflGameOracleSyncListener);
        // UserOracle.getInstance().unregisterSyncListener(mUserOracleSyncListener);
    }

    public void updateState() {
        Log.d(TAG, "updateState()");
        mQuarter.setText(Util.formatQuarter(getResources(), mGame.getQuarter()));
        mClock.setText(mGame.getClock());

        Log.d(TAG, mGame.getAwayTeam().getAbbr());
        Log.d(TAG, mGame.getHomeTeam().getAbbr());
        mAwayAbbr.setText(mGame.getAwayTeam().getAbbr());
        // mAwayAbbr.setTextColor(0xffffffff);
        Log.d(TAG, "getText(): " + mAwayAbbr.getText().toString());
        mAwayName.setText(mGame.getAwayTeam().getName());
        mHomeAbbr.setText(mGame.getHomeTeam().getAbbr());
        mHomeName.setText(mGame.getHomeTeam().getName());

        mAwayScore.setText(String.valueOf(mGame.getAwayTeam().getScore()));
        mHomeScore.setText(String.valueOf(mGame.getHomeTeam().getScore()));
    }

    private void confirmPrediction() {

    }

    private void changeScoreboardColor(int color) {
        mToolbar.setBackgroundColor(color);
        mScoreboardContainer.setBackgroundColor(color);
        mTabs.setBackgroundColor(color);
    }

    public NflGame getGame() {
        return mGame;
    }
}
