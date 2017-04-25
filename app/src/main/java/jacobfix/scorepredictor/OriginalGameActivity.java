package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.components.FlipCardView;
import jacobfix.scorepredictor.database.Database;
import jacobfix.scorepredictor.sync.NflGameSyncManager;
import jacobfix.scorepredictor.sync.SyncFinishedListener;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class OriginalGameActivity extends AppCompatActivity implements SyncFinishedListener,
        NumberPadFragment.NumberPadFragmentListener,
        GameProvider {

    private static final String TAG = OriginalGameActivity.class.getSimpleName();

    private NflGame mGame;

    private Toolbar mToolbar;

    private OriginalScoreboardView mScoreboard;
    private FlipCardView mAwayFlipCard;
    private FlipCardView mHomeFlipCard;

    private FlipCardView mSelectedFlipCard;

    private LinearLayout mPregameFinalContainer;
    private LinearLayout mClockQuarterContainer;
    private TextView mPregameFinal;
    private TextView mClock;
    private TextView mQuarter;
    private ImageView mLock;
    private FrameLayout mSuccessFailureContainer;
    private ImageView mSuccess;
    private ImageView mFailure;

    private LinearLayout mAwayTeamNameContainer;
    private TextView mAwayTeamAbbr;
    private TextView mAwayTeamName;
    private TextView mAwayTeamScore;
    private PredictionView mAwayTeamPrediction;
    private View mUpperAwayTeamStripe;
    private View mLowerAwayTeamStripe;
    private SpreadView mAwayTeamSpread;

    private LinearLayout mHomeTeamNameContainer;
    private TextView mHomeTeamAbbr;
    private TextView mHomeTeamName;
    private TextView mHomeTeamScore;
    private PredictionView mHomeTeamPrediction;
    private View mUpperHomeTeamStripe;
    private View mLowerHomeTeamStripe;
    private SpreadView mHomeTeamSpread;

    private RelativeLayout mSpreadContainer;
    private TextView mTotalSpread;
    private PredictionView mSelectedPrediction;

    private boolean mPredictSession;

    private NumberPadFragment mNumberPadFragment;
    private PlayFeedFragment mPlayFeedFragment;
    private StatsFragment mStatsFragment;
    private CompareFragment mCompareFragment;

    private TabLayout mTabs;
    private ViewPager mPager;
    private GamePagerAdapter mPagerAdapter;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private Database mDatabase;
    private NflGameSyncManager mNflGameSyncManager;
    
    private int mDefaultTextColor;
    private int mDefaultBackgroundColor;

    private static final String GAME_ID_EXTRA = "game_id";

    private static final int MAX_PREDICTION_DIGITS = 2;
    private static final String STATE_GAME_ID = "game_id";

    private static final int MIDDLE_CLOCK = 1;
    private static final int MIDDLE_PREGAME_FINAL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_material);

        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState is not null");
        }

        String gameId = getIntent().getStringExtra(GAME_ID_EXTRA);
        mGame = NflGameOracle.getInstance().getActiveGame(gameId);

        // Put the listener in the call to getNflGameSyncManager?
        // mNflGameSyncManager = ApplicationContext.getInstance(this).getNflGameSyncManager();
        // mNflGameSyncManager.setListener(this);
        // mNflGameSyncManager = ApplicationContext.getInstance(this).getNflGameSyncManager();

        mDatabase = new Database(this);
        mDatabase.open();
        // Every time a new one is created, the dataset gets updated immediately. Do we want this?

        // 70% saturation to get light from dark?
        // Log.d(TAG, getColorInfoString(this, android.R.holo_blue_dark);
        // Log.d(TAG, getColorInfoString(this, android.R.holo_blue_light);

        initializeActionBar();
        initializeViews();
        initializeTabsAndPager();

        if (mGame.isPredicted()) {
            colorPredictedWinner();
        }
        // initializeFragments();
        // initializeNavigationDrawer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_refresh:
                mNflGameSyncManager.sync();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // this.drawerToggle.syncState();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_GAME_ID, mGame.getGameId());
        Log.d(TAG, "Saving instance state");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        Log.d(TAG, "Parent: " + parent + ", Name: " + name);
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Sync should begin here
        mNflGameSyncManager.startScheduledSync(this);
        // this.nflGameSyncManager.start(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Log.d(TAG, "Restoring state");
            mGame = NflGameOracle.getInstance().getActiveGame(savedInstanceState.getString(STATE_GAME_ID));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNflGameSyncManager.stopScheduledSync(this);
        // mDatabase.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNflGameSyncManager.isRunning()) {
            mNflGameSyncManager.stopScheduledSync(this);
        }
        mDatabase.close();
    }

    private void initializeActionBar() {
        mToolbar = ViewUtil.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // getSupportActionBar().setCustomView(R.layout.game_title_view);
        /*
        TextView title = ViewUtil.findById(getSupportActionBar().getCustomView(), R.id.title);
        title.setTypeface(FontHelper.getYellowTailRegular(this));
        title.setText("ScorePrognosticator");
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        */
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initializeViews() {
        mScoreboard = ViewUtil.findById(this, R.id.scoreboard);
        mAwayFlipCard = mScoreboard.getAwayFlipCard();
        mHomeFlipCard = mScoreboard.getHomeFlipCard();

        View.OnClickListener onFlipCardClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mGame.isPregame()) {
                    return;
                }

                if (mSelectedFlipCard != null) {
                    // If a flip card is already selected...
                    if (mSelectedFlipCard == view) {
                        return;
                    }
                    confirmPrediction();
                }
                mSelectedFlipCard = (FlipCardView) view;
            }
        };

        mAwayFlipCard.setOnClickListener(onFlipCardClickedListener);
        mHomeFlipCard.setOnClickListener(onFlipCardClickedListener);

        // mScoreboard.update(mGame);
        /*
        mDefaultTextColor = mAwayTeamAbbr.getTextColors().getDefaultColor();
        mDefaultBackgroundColor = ContextCompat.getColor(this, android.R.color.transparent); // ((ColorDrawable) mAwayTeamNameContainer.getBackground()).getColor();

        mAwayTeamPrediction.setScorer(mGame.getAwayTeam());
        mHomeTeamPrediction.setScorer(mGame.getHomeTeam());

        mAwayTeamPrediction.setColor(mGame.getAwayTeam().getPrimaryColor());
        mHomeTeamPrediction.setColor(mGame.getHomeTeam().getPrimaryColor());
        
        // mAwayTeamPrediction.setBaseColor()

        mUpperAwayTeamStripe.setBackgroundColor(mGame.getAwayTeam().getPrimaryColor());
        mUpperHomeTeamStripe.setBackgroundColor(mGame.getHomeTeam().getPrimaryColor());
        mLowerAwayTeamStripe.setBackgroundColor(mGame.getAwayTeam().getPrimaryColor());
        mLowerHomeTeamStripe.setBackgroundColor(mGame.getHomeTeam().getPrimaryColor());

        mAwayTeamPrediction.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX, mAwayTeamScore.getTextSize());
        mHomeTeamPrediction.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_PX, mHomeTeamScore.getTextSize());

        mAwayTeamPrediction.resize();
        mHomeTeamPrediction.resize();

        mAwayTeamPrediction.refresh();
        mHomeTeamPrediction.refresh();

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Prediction block clicked");
                if (!mGame.isPregame()) {
                    return;
                }
                if (mSelectedPrediction != null) {
                    if (mSelectedPrediction == view) {
                        return;
                    }
                    confirmPrediction();
                }
                mSelectedPrediction = (PredictionView) view;
                NflTeam team = (NflTeam) mSelectedPrediction.getScorer();
                // Add a direction to the NumberPadFragment
                showNumberPadFragment(team.getLocale() + " " + team.getName(), team.getPrimaryColor(), !team.isHome());
            }
        };

        mAwayTeamPrediction.setOnClickListener(onClickListener);
        mHomeTeamPrediction.setOnClickListener(onClickListener);

        updateGameStateViews(mGame);
        */
    }

    private void initializeTabsAndPager() {
        mPager = ViewUtil.findById(this, R.id.pager);
        mPagerAdapter = new GamePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mTabs = ViewUtil.findById(this, R.id.tabs);
        mTabs.setupWithViewPager(mPager);

        mPlayFeedFragment = (PlayFeedFragment) mPagerAdapter.getItem(mPagerAdapter.LIVE_PAGE);
        mStatsFragment = (StatsFragment) mPagerAdapter.getItem(mPagerAdapter.STATS_PAGE);
        mCompareFragment = (CompareFragment) mPagerAdapter.getItem(mPagerAdapter.COMPARE_PAGE);
    }

    private void showNumberPadFragment(String headerText, int headerColor, boolean fromLeft) {
        mNumberPadFragment = NumberPadFragment.newInstance(headerText, headerColor);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fromLeft) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
        }
        // fragmentTransaction.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top);
        fragmentTransaction.add(R.id.container, mNumberPadFragment);
        fragmentTransaction.commit();
        Log.d(TAG, "Number pad should be displayed");
    }

    private void hideNumberPadFragment(boolean fromLeft) {
        Log.d(TAG, "About to hide fragment");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fromLeft) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);
        }
        fragmentTransaction.remove(mNumberPadFragment);
        fragmentTransaction.commit();
        mNumberPadFragment = null;
        Log.d(TAG, "Number pad should no longer be displayed");
    }

    private void updateGameStateViews(NflGame game) {
        // Async task to check if things are different?
        // Only if updates are noticeable
        if (game.isPregame()) {
            // showScores(false)?
            showPredictions(true);
            showSpreads(false);
            setMiddleContents(MIDDLE_PREGAME_FINAL);
            showSuccessFailure(false);
            mPregameFinal.setText(R.string.pregame);
        } else {
            showScores(true);

            if (!mGame.isPredicted()) {
                showPredictions(false);
                showSpreads(false);
                showSuccessFailure(false);
            }

            if (game.inProgress()) {
                setMiddleContents(MIDDLE_CLOCK);
            } else if (game.isFinal()) {
                setMiddleContents(MIDDLE_PREGAME_FINAL);
                mPregameFinal.setText(R.string.final_);
                if (mGame.isPredicted()) {
                    showSuccessFailure(true);
                    boolean correct = mGame.wasPredictedCorrectly();
                    if (correct) {
                        indicateSuccess(true);
                    } else {
                        indicateSuccess(false);
                    }
                }
            }
        }

        mClock.setText(game.getClock());
        mQuarter.setText(Util.formatQuarter(getResources(), game.getQuarter()));

        mAwayTeamAbbr.setText(game.getAwayTeam().getAbbr());
        mAwayTeamName.setText(game.getAwayTeam().getName());
        mAwayTeamScore.setText(String.valueOf(game.getAwayTeam().getScore()));
        // ViewUtil.findById(this, R.id.away_team_stripe).setBackgroundColor(game.getAwayTeam().getPrimaryColor());

        mHomeTeamAbbr.setText(game.getHomeTeam().getAbbr());
        mHomeTeamName.setText(game.getHomeTeam().getName());
        mHomeTeamScore.setText(String.valueOf(game.getHomeTeam().getScore()));
        // ViewUtil.findById(this, R.id.home_team_stripe).setBackgroundColor(game.getHomeTeam().getPrimaryColor());

        mAwayTeamSpread.compute(mGame.getAwayTeam());
        mHomeTeamSpread.compute(mGame.getHomeTeam());
        mTotalSpread.setText(String.valueOf(mAwayTeamSpread.getValue() + mHomeTeamSpread.getValue()));
    }

    private void findViews() {
        mScoreboard = ViewUtil.findById(this, R.id.scoreboard);

        mPregameFinalContainer = ViewUtil.findById(this, R.id.pregame_final_container);
        mClockQuarterContainer = ViewUtil.findById(this, R.id.clock_quarter_container);
        mPregameFinal = ViewUtil.findById(this, R.id.pregame_final);
        mClock = ViewUtil.findById(this, R.id.clock);
        mQuarter = ViewUtil.findById(this, R.id.quarter);
        mLock = ViewUtil.findById(this, R.id.lock);

        mSuccessFailureContainer = ViewUtil.findById(this, R.id.success_failure_container);
        mSuccess = ViewUtil.findById(this, R.id.success);
        mFailure = ViewUtil.findById(this, R.id.failure);

        mAwayTeamNameContainer = ViewUtil.findById(this, R.id.away_team_name_container);
        mHomeTeamNameContainer = ViewUtil.findById(this, R.id.home_team_name_container);

        mAwayTeamAbbr = ViewUtil.findById(this, R.id.away_team_abbr);
        mHomeTeamAbbr = ViewUtil.findById(this, R.id.home_team_abbr);

        mAwayTeamName = ViewUtil.findById(this, R.id.away_team_name);
        mHomeTeamName = ViewUtil.findById(this, R.id.home_team_name);

        mAwayTeamScore = ViewUtil.findById(this, R.id.away_team_score);
        mHomeTeamScore = ViewUtil.findById(this, R.id.home_team_score);

        mAwayTeamPrediction = ViewUtil.findById(this, R.id.away_team_prediction_container);
        mHomeTeamPrediction = ViewUtil.findById(this, R.id.home_team_prediction_container);

        mUpperAwayTeamStripe = ViewUtil.findById(this, R.id.upper_away_stripe);
        mUpperHomeTeamStripe = ViewUtil.findById(this, R.id.upper_home_stripe);

        mLowerAwayTeamStripe = ViewUtil.findById(this, R.id.lower_away_stripe);
        mLowerHomeTeamStripe = ViewUtil.findById(this, R.id.lower_home_stripe);

        mSpreadContainer = ViewUtil.findById(this, R.id.lower_middle_container);
        mTotalSpread = ViewUtil.findById(this, R.id.total_spread);
        mAwayTeamSpread = ViewUtil.findById(this, R.id.away_team_spread);
        mHomeTeamSpread = ViewUtil.findById(this, R.id.home_team_spread);
    }

    private void setTypefaces() {
        mPregameFinal.setTypeface(FontHelper.getArimoRegular(this));

        mClock.setTypeface(FontHelper.getArimoRegular(this));
        mQuarter.setTypeface(FontHelper.getArimoRegular(this));

        mAwayTeamAbbr.setTypeface(FontHelper.getWorkSansBold(this));
        mHomeTeamAbbr.setTypeface(FontHelper.getWorkSansBold(this));

        mAwayTeamName.setTypeface(FontHelper.getWorkSansBold(this));
        mHomeTeamName.setTypeface(FontHelper.getWorkSansBold(this));

        mAwayTeamScore.setTypeface(FontHelper.getArimoRegular(this));
        mHomeTeamScore.setTypeface(FontHelper.getArimoRegular(this));

        mAwayTeamPrediction.getTextView().setTypeface(FontHelper.getArimoRegular(this));
        mHomeTeamPrediction.getTextView().setTypeface(FontHelper.getArimoRegular(this));

        mTotalSpread.setTypeface(FontHelper.getArimoRegular(this));
        mAwayTeamSpread.getTextView().setTypeface(FontHelper.getArimoRegular(this));
        mHomeTeamSpread.getTextView().setTypeface(FontHelper.getArimoRegular(this));
    }

    /*
    private void initializeNavigationDrawer() {
        this.drawerLayout = ViewUtil.findById(this, R.id.drawer_layout);
        this.drawerToggle = new ActionBarDrawerToggle(this, this.drawerLayout, R.string.drawer_open, R.string.drawer_closed) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        this.drawerToggle.setDrawerIndicatorEnabled(true);
        this.drawerLayout.addDrawerListener(drawerToggle);
    } */

    public void onSyncFinished() {
        Log.d(TAG, "onSyncFinished() called in OriginalGameActivity");
        NflGame updatedGame = NflGameOracle.getInstance().getActiveGame(mGame.getGameId());
        if (updatedGame == null) {
            // archive(this.game);
        } else {
            mGame = updatedGame;
            updateGameStateViews(mGame);
            Log.d(TAG, "Updating fragment");
            // We can't update the fragment until everything loads
            if (mPlayFeedFragment.isAdded()) { // isAdded
                mPlayFeedFragment.update();
            }
        }
        // Toast.makeText(this, OriginalGameActivity.class.getSimpleName() + ": Data set updated for " + mGame.getGameId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void keyPressed(NumberPadFragment.Key k) {
        if (mSelectedPrediction == null) {
            return;
        }
        
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

    @Override
    public NflGame getGame() {
        return mGame;
    }

    private void addDigitToPrediction(String digit) {
        if (!mPredictSession) {
            clearPrediction();
            mPredictSession = true;
        }
        if (mSelectedPrediction.getTextView().length() < MAX_PREDICTION_DIGITS) {
            mSelectedPrediction.append(digit);
        }
    }
    
    private void clearPrediction() {
        mSelectedPrediction.clear();
    }

    private void colorPredictedWinner() {
        NflTeam predictedWinner = mGame.getPredictedWinner();
        if (predictedWinner == null) {
            mHomeTeamNameContainer.setBackgroundColor(mDefaultBackgroundColor);
            mHomeTeamAbbr.setTextColor(mDefaultTextColor);
            mHomeTeamName.setTextColor(mDefaultTextColor);
            mAwayTeamNameContainer.setBackgroundColor(mDefaultBackgroundColor);
            mAwayTeamAbbr.setTextColor(mDefaultTextColor);
            mAwayTeamName.setTextColor(mDefaultTextColor);
        } else if (predictedWinner.isHome()) {
            mHomeTeamNameContainer.setBackgroundColor(mGame.getHomeTeam().getPrimaryColor());
            mHomeTeamAbbr.setTextColor(Color.WHITE);
            mHomeTeamName.setTextColor(Color.WHITE);
            mAwayTeamNameContainer.setBackgroundColor(mDefaultBackgroundColor);
            mAwayTeamAbbr.setTextColor(mDefaultTextColor);
            mAwayTeamName.setTextColor(mDefaultTextColor);
        } else {
            mAwayTeamNameContainer.setBackgroundColor(mGame.getAwayTeam().getPrimaryColor());
            mAwayTeamAbbr.setTextColor(Color.WHITE);
            mAwayTeamName.setTextColor(Color.WHITE);
            mHomeTeamNameContainer.setBackgroundColor(mDefaultBackgroundColor);
            mHomeTeamAbbr.setTextColor(mDefaultTextColor);
            mHomeTeamName.setTextColor(mDefaultTextColor);
        }
    }

    private void confirmPrediction() {
        new AsyncTask<Void, Void, Boolean>() {

            String gameId;
            boolean isHome;
            int prediction;

            @Override
            public void onPreExecute() {
                gameId = mGame.getGameId();
                isHome = ((NflTeam) mSelectedPrediction.getScorer()).isHome();
                prediction = mSelectedPrediction.getScorer().getPredictedScore();
            }

            @Override
            public Boolean doInBackground(Void... nothing) {
                mDatabase.updatePrediction(gameId, isHome, prediction);
                return true;
            }
        }.execute();
        hideNumberPadFragment(!((NflTeam) mSelectedPrediction.getScorer()).isHome());
        mSelectedPrediction = null;
        mPredictSession = false;
        if (mGame.isPredicted()) {
            colorPredictedWinner();
        }
    }

    private void showScores(boolean show) {
        if (show) {
            mAwayTeamScore.setVisibility(View.VISIBLE);
            mHomeTeamScore.setVisibility(View.VISIBLE);
        } else {
            mAwayTeamScore.setVisibility(View.GONE);
            mHomeTeamScore.setVisibility(View.GONE);
        }
    }

    private void showPredictions(boolean show) {
        if (show) {
            mAwayTeamPrediction.setVisibility(View.VISIBLE);
            mHomeTeamPrediction.setVisibility(View.VISIBLE);
        } else {
            mAwayTeamPrediction.setVisibility(View.GONE);
            mHomeTeamPrediction.setVisibility(View.GONE);
        }
    }

    private void showSpreads(boolean show) {
        if (show) {
            mTotalSpread.setVisibility(View.VISIBLE);
            mAwayTeamSpread.setVisibility(View.VISIBLE);
            mHomeTeamSpread.setVisibility(View.VISIBLE);
        } else {
            mTotalSpread.setVisibility(View.GONE);
            mAwayTeamSpread.setVisibility(View.GONE);
            mHomeTeamSpread.setVisibility(View.GONE);
        }
    }

    private void setMiddleContents(int indicator) {
        switch (indicator) {
            case MIDDLE_CLOCK:
                mClockQuarterContainer.setVisibility(View.VISIBLE);
                mPregameFinalContainer.setVisibility(View.GONE);
                break;
            case MIDDLE_PREGAME_FINAL:
                mClockQuarterContainer.setVisibility(View.GONE);
                mPregameFinalContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showSuccessFailure(boolean show) {
        if (show) {
            mSuccessFailureContainer.setVisibility(View.VISIBLE);
        } else {
            mSuccessFailureContainer.setVisibility(View.GONE);
        }
    }

    private void indicateSuccess(boolean success) {
        if (success) {
            mSuccess.setVisibility(View.VISIBLE);
            mFailure.setVisibility(View.GONE);
        } else {
            mSuccess.setVisibility(View.GONE);
            mFailure.setVisibility(View.VISIBLE);
        }
    }
}
