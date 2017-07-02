package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import jacobfix.scorepredictor.sync.NflGameOracle;
import jacobfix.scorepredictor.sync.SyncListener;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.RankUsersTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class GameActivity extends AppCompatActivity implements GameProvider, NumberPadFragment.NumberPadFragmentListener {

    private static final String TAG = GameActivity.class.getSimpleName();

    private NflGame mGame;
    private ArrayList<User> mRankedParticipants;

    private Toolbar mToolbar;

    private RelativeLayout mUpperScoreboard;
    private RelativeLayout mLowerScoreboard;

    private LinearLayout mUpperMiddleContainer;
    private LinearLayout mLowerMiddleContainer;

    private RelativeLayout mAwayScoreBlock;
    private RelativeLayout mHomeScoreBlock;
    private FrameLayout mAwayPredictionBlock;
    private FrameLayout mHomePredictionBlock;

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
    
    private PredictionView mAwayFlipCard;
    private PredictionView mHomeFlipCard;

    private PredictionView mSelectedFlipCard;
    private NflTeam mSelectedTeam;
    private boolean mPredictSession;

    private TabLayout mTabs;
    private ViewPager mPager;
    private GamePagerAdapter mPagerAdapter;

    private SyncListener mNflGameOracleSyncListener;
    // private SyncListener mUserOracleSyncListener;
    private SyncListener mUserSyncListener;

    private FriendPredictionFragment mFriendPredictionFragment;
    private NumberPadFragment mNumberPadFragment;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final String GAME_ID_EXTRA = "game_id";

    private static final int NEITHER_TAG = 0;
    private static final int AWAY_TAG = 1;
    private static final int HOME_TAG = 2;

    private static final int MAX_PREDICTION_DIGITS = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_material_triple_new);

        /* This activity must be passed a game ID on creation. */
        String gameId = getIntent().getStringExtra(GAME_ID_EXTRA);
        if (gameId == null) Log.e(TAG, GameActivity.class.getSimpleName() + " was started without a game ID");
        mGame = NflGameOracle.getInstance().getActiveGame(gameId);
        if (mGame == null && (mGame = NflGameOracle.getInstance().getArchivedGame(gameId)) == null) {
            /* There's a problem. */
        }

        mRankedParticipants = new ArrayList<>();

        initializeActionBar();
        initializeViews();
        initializeTabsAndPager();
        initializeListeners();
        updateState();
        // changeScoreboardColor(mGame.getAwayTeam().getPrimaryColor());
        changeScoreboardColor(ContextCompat.getColor(this, android.R.color.darker_gray));
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
        mUpperScoreboard = ViewUtil.findById(this, R.id.upper_scoreboard_container);
        mLowerScoreboard = ViewUtil.findById(this, R.id.lower_scoreboard_container);

        mUpperMiddleContainer = ViewUtil.findById(this, R.id.upper_middle_container);
        mLowerMiddleContainer = ViewUtil.findById(this, R.id.lower_middle_container);

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

        mAwayFlipCard.getTextView().setTypeface(FontHelper.getYantramanavRegular(this));
        mHomeFlipCard.getTextView().setTypeface(FontHelper.getYantramanavRegular(this));

        // mAwayFlipCard.getTextView().setTextColor(mGame.getAwayTeam().getPrimaryColor());
        // mAwayFlipCard.getTextView().setText("26");
        // mAwayFlipCard.getTextView().setTypeface(FontHelper.getYantramanavBold(this));
        // mHomeFlipCard.hideBackground();
        // mHomeFlipCard.getTextView().setText("14");
        // mHomeFlipCard.getTextView().setTextColor(mGame.getAwayTeam().getPrimaryColor());
        // mHomeFlipCard.setColor(0x40ffffff);
        // mHomeFlipCard.hideBackground();

        ViewUtil.applyDeboss(mAwayFlipCard.getTextView());
        ViewUtil.applyDeboss(mHomeFlipCard.getTextView());

        mAwayFlipCard.transparentBackground(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white));
        mHomeFlipCard.transparentBackground(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white));

        View.OnClickListener onTeamClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* if (!mGame.isPregame()) {
                    return;
                } */

                Log.d(TAG, "CLICKED");

                PredictionView touched = null;
                NflTeam team = null;
                switch ((int) view.getTag()) {
                    case AWAY_TAG:
                        touched = mAwayFlipCard;
                        team = mGame.getAwayTeam();
                        break;
                    case HOME_TAG:
                        touched = mHomeFlipCard;
                        team = mGame.getHomeTeam();
                        break;
                }

                if (mSelectedFlipCard != null) {
                    /* If there is an already selected flip card... */
                    if (touched == mSelectedFlipCard) {
                        return;
                    }
                    confirmPrediction();
                }
                mSelectedFlipCard = touched;
                mSelectedTeam = team;
                // showNumberPadFragment(mSelectedTeam, (int) view.getTag() == AWAY_TAG);
                NflTeam predictedWinner = getPredictedWinner();
                int bufferColor;
                if (predictedWinner == null)
                    bufferColor = ContextCompat.getColor(GameActivity.this, android.R.color.darker_gray);
                else
                    bufferColor = predictedWinner.getPrimaryColor();
                showNumberPadFragment(team, bufferColor, !team.isHome());
            }
        };

        mAwayScoreBlock = ViewUtil.findById(this, R.id.away_block);
        mAwayScoreBlock.setTag(AWAY_TAG);
        mAwayScoreBlock.setOnClickListener(onTeamClickedListener);

        mHomeScoreBlock = ViewUtil.findById(this, R.id.home_block);
        mHomeScoreBlock.setTag(HOME_TAG);
        mHomeScoreBlock.setOnClickListener(onTeamClickedListener);

        mAwayPredictionBlock = ViewUtil.findById(this, R.id.away_prediction_block);
        mAwayPredictionBlock.setTag(AWAY_TAG);
        mAwayPredictionBlock.setOnClickListener(onTeamClickedListener);

        mHomePredictionBlock = ViewUtil.findById(this, R.id.home_prediction_block);
        mHomePredictionBlock.setTag(HOME_TAG);
        mHomePredictionBlock.setOnClickListener(onTeamClickedListener);
    }

    private void initializeTabsAndPager() {
        mPager = ViewUtil.findById(this, R.id.pager);
        mPagerAdapter = new GamePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mTabs = ViewUtil.findById(this, R.id.tabs);
        mTabs.setupWithViewPager(mPager);
        mTabs.setSelectedTabIndicatorColor(Color.WHITE);

        // mFriendPredictionFragment = (FriendPredictionFragment) mPagerAdapter.getItem(mPagerAdapter.FRIENDS_PAGE);


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
        mUserSyncListener = new SyncListener() {
            @Override
            public void onSyncFinished() {
                Log.d(TAG, "User sync finished");
                Collection<User> participants = UserSyncManager.getInstance().getParticipatingFriends(mGame.getGameId());
                participants.add(UserSyncManager.getInstance().me());

                Log.d(TAG, "About to start RankUsersTask");
                new RankUsersTask(participants, mGame, new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        mRankedParticipants = (ArrayList<User>) task.getResult();
                        FriendPredictionFragment fragment = (FriendPredictionFragment) mPagerAdapter.getRegisteredFragment(GamePagerAdapter.FRIENDS_PAGE);
                        fragment.setFriends(mRankedParticipants);
                    }
                }).start();

                /* ArrayList<User> asList = new ArrayList<>();
                for (User user : participants) {
                    asList.add(user);
                }
                mRankedParticipants = asList;

                FriendPredictionFragment fragment = (FriendPredictionFragment) mPagerAdapter.getRegisteredFragment(GamePagerAdapter.FRIENDS_PAGE);
                fragment.setFriends(asList); */

                // TODO: We should be able to reset the users to sync to a much smaller set, i.e., only the users playing this game
            }

            @Override
            public void onSyncError() {
                Log.d(TAG, "User sync error");
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

        UserSyncManager.getInstance().setPredictionsOfBackgroundSync(Collections.singletonList(mGame.getGameId()));
        UserSyncManager.getInstance().registerSyncListener(mUserSyncListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        NflGameOracle.getInstance().unregisterSyncListener(mNflGameOracleSyncListener);
        UserSyncManager.getInstance().unregisterSyncListener(mUserSyncListener);
    }

    public void updateState() {
        Log.d(TAG, "updateState()");
        Log.d(TAG, String.valueOf(mGame.getQuarter()));
        Log.d(TAG, Util.formatQuarter(getResources(), mGame.getQuarter()));
        if (mGame.isFinal()) {
            mQuarter.setText("FINAL");
            mClock.setVisibility(View.GONE);
        } else {
            mQuarter.setText(Util.formatQuarter(getResources(), mGame.getQuarter()));
            mClock.setText(mGame.getClock());
        }

        mAwayAbbr.setText(mGame.getAwayTeam().getAbbr());
        mAwayName.setText(mGame.getAwayTeam().getName());
        mHomeAbbr.setText(mGame.getHomeTeam().getAbbr());
        mHomeName.setText(mGame.getHomeTeam().getName());

        mAwayScore.setText(String.valueOf(mGame.getAwayTeam().getScore()));
        mHomeScore.setText(String.valueOf(mGame.getHomeTeam().getScore()));
    }

    private void showNumberPadFragment(NflTeam team, int bufferColor, boolean fromLeft) {
        Log.d(TAG, "Show number pad fragment");
        mNumberPadFragment = NumberPadFragment.newInstance(team.getLocale() + " " + team.getName(), team.getPrimaryColor(), bufferColor);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (fromLeft)
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

        fragmentTransaction.add(R.id.container, mNumberPadFragment);
        fragmentTransaction.commit();
    }

    private void hideNumberPadFragment(boolean fromLeft) {
        Log.d(TAG, "About to hide fragment");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fromLeft)
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
        fragmentTransaction.remove(mNumberPadFragment);
        fragmentTransaction.commit();
        mNumberPadFragment = null;
        Log.d(TAG, "Number pad should no longer be displayed");
    }

    private boolean isPredicted() {
        return mAwayFlipCard.getScore() != -1 && mHomeFlipCard.getScore() != -1;
    }

    private NflTeam getPredictedWinner() {
        if (!isPredicted())
            return null;
        if (mAwayFlipCard.getScore() > mHomeFlipCard.getScore())
            return mGame.getAwayTeam();
        else if (mAwayFlipCard.getScore() < mHomeFlipCard.getScore())
            return mGame.getHomeTeam();
        else
            return null;
    }

    private void confirmPrediction() {
        int bufferColor;
        if (isPredicted()) {
            markPredictedWinner();
            bufferColor = getPredictedWinner().getPrimaryColor();
        } else {
            bufferColor = ContextCompat.getColor(this, android.R.color.darker_gray);
        }
        mNumberPadFragment.setBufferColor(bufferColor);
        hideNumberPadFragment(!mSelectedTeam.isHome());
        mSelectedFlipCard = null;
        mSelectedTeam = null;
        mPredictSession = false;
    }

    private void clearPrediction() {
        mSelectedFlipCard.clear();
    }

    private void addDigitToPrediction(String digit) {
        if (!mPredictSession) {
            clearPrediction();
            mPredictSession = true;
        }
        if (mSelectedFlipCard.getTextView().length() < MAX_PREDICTION_DIGITS)
            mSelectedFlipCard.append(digit);
    }

    private void markPredictedWinner() {
        if (mAwayFlipCard.getScore() > mHomeFlipCard.getScore()) {
            mAwayFlipCard.solidBackground(mGame.getAwayTeam().getPrimaryColor(), ContextCompat.getColor(this, android.R.color.white));
            mHomeFlipCard.transparentBackground(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white));
            changeScoreboardColor(mGame.getAwayTeam().getPrimaryColor());
        } else if (mAwayFlipCard.getScore() < mHomeFlipCard.getScore()) {
            mAwayFlipCard.transparentBackground(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white));
            mHomeFlipCard.solidBackground(mGame.getHomeTeam().getPrimaryColor(), ContextCompat.getColor(this, android.R.color.white));
            changeScoreboardColor(mGame.getHomeTeam().getPrimaryColor());
        } else {
            mAwayFlipCard.transparentBackground(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white));
            mHomeFlipCard.transparentBackground(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void changeScoreboardColor(int color) {
        mToolbar.setBackgroundColor(color);
        mUpperScoreboard.setBackgroundColor(color);
        mLowerScoreboard.setBackgroundColor(color);
        mTabs.setBackgroundColor(color);
    }

    @Override
    public NflGame getGame() {
        return mGame;
    }

    @Override
    public ArrayList<User> getRankedParticipants() {
        return mRankedParticipants;
    }

    @Override
    public void keyPressed(NumberPadFragment.Key k) {
        if (mSelectedFlipCard == null)
            return;

        switch (k) {
            case KEY_ENTER:
                confirmPrediction();
                break;
            case KEY_DELETE:
                clearPrediction();
                break;
            case KEY_0:
                addDigitToPrediction("0");
                break;
            case KEY_1:
                addDigitToPrediction("1");
                break;
            case KEY_2:
                addDigitToPrediction("2");
                break;
            case KEY_3:
                addDigitToPrediction("3");
                break;
            case KEY_4:
                addDigitToPrediction("4");
                break;
            case KEY_5:
                addDigitToPrediction("5");
                break;
            case KEY_6:
                addDigitToPrediction("6");
                break;
            case KEY_7:
                addDigitToPrediction("7");
                break;
            case KEY_8:
                addDigitToPrediction("8");
                break;
            case KEY_9:
                addDigitToPrediction("9");
                break;
        }
    }
}
