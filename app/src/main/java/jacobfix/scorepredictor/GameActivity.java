package jacobfix.scorepredictor;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jacobfix.scorepredictor.components.FlipCardView;
import jacobfix.scorepredictor.friends.User;
import jacobfix.scorepredictor.friends.UserOracle;
import jacobfix.scorepredictor.sync.SyncFinishedListener;
import jacobfix.scorepredictor.util.ViewUtil;

public class GameActivity extends AppCompatActivity implements GameProvider {

    private static final String TAG = GameActivity.class.getSimpleName();

    private NflGame mGame;
    private ArrayList<User> mRankedParticipants;

    private Toolbar mToolbar;
    private ScoreboardView mScoreboard;

    private FlipCardView mSelectedFlipCard;

    private TabLayout mTabs;
    private ViewPager mPager;
    private GamePagerAdapter mPagerAdapter;

    private SyncFinishedListener mNflGameOracleSyncListener;
    private SyncFinishedListener mUserOracleSyncListener;

    private FriendPredictionFragment mFriendPredictionFragment;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final String GAME_ID_EXTRA = "game_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_material_new);

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
    }

    private void initializeActionBar() {
        mToolbar = ViewUtil.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initializeViews() {
        mScoreboard = ViewUtil.findById(this, R.id.scoreboard);
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
        mScoreboard.getAwayFlipCard().setOnClickListener(onFlipCardClickedListener);
        mScoreboard.getHomeFlipCard().setOnClickListener(onFlipCardClickedListener);
    }

    private void initializeTabsAndPager() {
        mPager = ViewUtil.findById(this, R.id.pager);
        mPagerAdapter = new GamePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mTabs = ViewUtil.findById(this, R.id.tabs);
        mTabs.setupWithViewPager(mPager);
    }

    private void initializeListeners() {
        mNflGameOracleSyncListener = new SyncFinishedListener() {
            @Override
            public void onSyncFinished() {
                NflGame updatedGame = NflGameOracle.getInstance().getActiveGame(mGame.getGameId());
                /* Might no longer be a part of the active games. */
                if (updatedGame == null) {
                    updatedGame = NflGameOracle.getInstance().getArchivedGame(mGame.getGameId());
                }
                mGame = updatedGame;
                mScoreboard.updateState(mGame);
            }
        };
        /* After the UserOracle syncs, we need to update the ranking of friends for this game. */
        mUserOracleSyncListener = new SyncFinishedListener() {
            @Override
            public void onSyncFinished() {
                Collection<User> friends = UserOracle.getInstance().getParticipatingFriends(mGame.getGameId());
                friends.add(UserOracle.getInstance().me());
                new RankUsersTask(friends, mGame.getGameId(), new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        mRankedParticipants = (ArrayList<User>) task.mResult;
                        mFriendPredictionFragment.setFriends(mRankedParticipants);
                    }
                });
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
        UserOracle.getInstance().registerSyncListener(mUserOracleSyncListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        NflGameOracle.getInstance().unregisterSyncListener(mNflGameOracleSyncListener);
        UserOracle.getInstance().unregisterSyncListener(mUserOracleSyncListener);
    }

    private void confirmPrediction() {

    }

    public NflGame getGame() {
        return mGame;
    }
}
