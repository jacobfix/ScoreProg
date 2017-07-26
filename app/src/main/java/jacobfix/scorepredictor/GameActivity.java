package jacobfix.scorepredictor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.sync.GameProvider;
import jacobfix.scorepredictor.sync.NflGameOracle;
import jacobfix.scorepredictor.sync.SyncListener;
import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class GameActivity extends AppCompatActivity implements NumberPadFragment.NumberPadFragmentListener {

    private static final String TAG = GameActivity.class.getSimpleName();

    private NflGame game;
    private Prediction prediction;

    private ArrayList<User> mRankedParticipants;

    private ProgressBar loadingSymbol;
    private Toolbar mToolbar;

    private LinearLayout scoreboard;
    private RelativeLayout mUpperScoreboard;
    private RelativeLayout mLowerScoreboard;

    private LinearLayout mUpperMiddleContainer;
    private SpreadView spread;

    private LinearLayout upperMiddleContainer;
    private LowerMiddleContainer lowerMiddleContainer;

    private ImageView lock;

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

    private int scoreboardColor;

    private BroadcastReceiver lockBroadcastReceiver;

    private SyncListener mNflGameOracleSyncListener;
    // private SyncListener mUserOracleSyncListener;
    // private SyncListener mUserSyncListener;

    private HashSet<GameStateChangeListener> gameStateChangeListeners = new HashSet<>();

    private AsyncCallback<Map<String, NflGame>> gameSyncListener;
    private AsyncCallback<User[]> detailsSyncListener;
    private AsyncCallback<Predictions[]> predictionsSyncListener;

    private FriendPredictionFragment mFriendPredictionFragment;
    private NumberPadFragment mNumberPadFragment;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final String GAME_ID_EXTRA = "game_id";
    public static final String ACTION_ANNOUNCE_GAME_LOCKED = "lock";

    private static final int NEITHER_TAG = 0;
    private static final int AWAY_TAG = 1;
    private static final int HOME_TAG = 2;

    private static final int MAX_PREDICTION_DIGITS = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_material_triple_new);

        /* This activity must be passed a game ID on creation. */
        final String gameId = getIntent().getStringExtra("game");
        if (gameId == null)
            Log.wtf(TAG, "GameActivity was started without a game ID");

        game = Schedule.getGame(gameId);
        if (game == null)
            Log.wtf(TAG, "Game did not exist in Schedule's collections of games");

        lockBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String gameId = intent.getStringExtra("game_id");
                if (gameId.equals(game.getGameId())) {
                    if (mNumberPadFragment != null)
                        hideNumberPadFragment(!mSelectedTeam.isHome());

                    updateDisplayedGameState();
                    updateDisplayedPredictions();
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(lockBroadcastReceiver, new IntentFilter(ACTION_ANNOUNCE_GAME_LOCKED));

        initializeActionBar();
        initializeViews();
        initializeTabsAndPager();
        initializeListeners();

        updateDisplayedGameState();

        changeScoreboardColor(ContextCompat.getColor(GameActivity.this, R.color.standard_text));
    }

    private void initializeActionBar() {
        mToolbar = ViewUtil.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initializeViews() {
        loadingSymbol = ViewUtil.findById(this, R.id.loading_circle);

        // mScoreboard = ViewUtil.findById(this, R.id.scoreboard);
        scoreboard = ViewUtil.findById(this, R.id.scoreboard);
        mUpperScoreboard = ViewUtil.findById(this, R.id.upper_scoreboard_container);
        mLowerScoreboard = ViewUtil.findById(this, R.id.lower_scoreboard_container);

        mUpperMiddleContainer = ViewUtil.findById(this, R.id.upper_middle_container);
        // spread = ViewUtil.findById(this, R.id.lower_middle_container);

        lock = ViewUtil.findById(this, R.id.lock);

        // upperMiddleContainer = ViewUtil.findById(this, R.id.upper_middle_container);
        lowerMiddleContainer = ViewUtil.findById(this, R.id.lower_middle_container);
        lowerMiddleContainer.setPredictionTextColor(Color.WHITE);
        lowerMiddleContainer.getSpreadView().setProgressBarColor(Color.WHITE);
        lowerMiddleContainer.getSpreadView().setTextColor(Color.WHITE);
        lowerMiddleContainer.showSpread();

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

        // mAwayFlipCard.getTextView().setTextColor(game.getAwayTeam().getPrimaryColor());
        // mAwayFlipCard.getTextView().setText("26");
        // mAwayFlipCard.getTextView().setTypeface(FontHelper.getYantramanavBold(this));
        // mHomeFlipCard.hideBackground();
        // mHomeFlipCard.getTextView().setText("14");
        // mHomeFlipCard.getTextView().setTextColor(game.getAwayTeam().getPrimaryColor());
        // mHomeFlipCard.setColor(0x40ffffff);
        // mHomeFlipCard.hideBackground();

        ViewUtil.applyDeboss(mAwayFlipCard.getTextView());
        ViewUtil.applyDeboss(mHomeFlipCard.getTextView());

        mAwayFlipCard.strokedBackground(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white));
        mHomeFlipCard.strokedBackground(ContextCompat.getColor(this, android.R.color.white), ContextCompat.getColor(this, android.R.color.white));

        View.OnClickListener onTeamClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (game.isLocked())
                    return;

                Log.d(TAG, "AWAY SCORE: " + prediction.getAwayScore());
                Log.d(TAG, "HOME SCORE: " + prediction.getHomeScore());

                Log.d(TAG, "CLICKED");

                PredictionView touched = null;
                NflTeam team = null;
                switch ((int) view.getTag()) {
                    case AWAY_TAG:
                        touched = mAwayFlipCard;
                        team = game.getAwayTeam();
                        break;
                    case HOME_TAG:
                        touched = mHomeFlipCard;
                        team = game.getHomeTeam();
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
                    bufferColor = ContextCompat.getColor(GameActivity.this, R.color.standard_text);
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
                NflGame updatedGame = NflGameOracle.getInstance().getActiveGame(game.getGameId());
                /* Might no longer be a part of the active games. */
                if (updatedGame == null) {
                    updatedGame = NflGameOracle.getInstance().getArchivedGame(game.getGameId());
                }
                game = updatedGame;
                // mScoreboard.updateState(game);
                updateDisplayedGameState();
            }

            @Override
            public void onSyncError() {

            }
        };

        gameSyncListener = new AsyncCallback<Map<String, NflGame>>() {
            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(Map<String, NflGame> result) {
                updateDisplayedGameState();
            }
        };

        /* After the UserOracle syncs, we need to update the ranking of friends for this game. */
        /* mUserSyncListener = new SyncListener() {
            @Override
            public void onSyncFinished() {
                Log.d(TAG, "User sync finished");
                Collection<User> participants = UserSyncManager.getInstance().getParticipatingFriends(game.getGameId());
                participants.add(UserSyncManager.getInstance().me());

                Log.d(TAG, "About to start RankUsersTask");
                new RankUsersTask(participants, game, new TaskFinishedListener() {
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

        /*        // TODO: We should be able to reset the users to sync to a much smaller set, i.e., only the users playing this game
            }

            @Override
            public void onSyncError() {
                Log.d(TAG, "User sync error");
            }
        }; */
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Pull existing prediction for this game from the server. */
        UserProvider.getUserPredictions(game.getGameId(), Collections.singletonList(LocalAccountManager.getId()), new AsyncCallback<Map<String, Predictions>>() {
            @Override
            public void onSuccess(Map<String, Predictions> result) {
                Predictions predictions = result.get(LocalAccountManager.getId());
                if (predictions != null) prediction = predictions.get(game.getGameId());
                else                     prediction = new Prediction(game.getGameId());

                updateDisplayedPredictions();

                loadingSymbol.setVisibility(View.GONE);
                scoreboard.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {

            }

        });

        /*
        UserProvider.getUserPredictions(game.getGameId(), Arrays.asList(LocalAccountManager.getId()), new AsyncCallback<Map<String, Predictions>>() {
            @Override
            public void onSuccess(Map<String, Predictions> result) {
                if (!result.isEmpty()) {
                    Predictions predictions = (Predictions) result.values().toArray()[0];
                    prediction = predictions.get(game.getGameId());
                    updateDisplayedPredictions();
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        */

        // Schedule.registerGameSyncListener(gameSyncListener);

        // NflGameOracle.getInstance().registerSyncListener(mNflGameOracleSyncListener);

        // UserSyncManager.getInstance().setPredictionsOfBackgroundSync(Collections.singletonList(game.getGameId()));
        // UserSyncManager.getInstance().registerSyncListener(mUserSyncListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // NflGameOracle.getInstance().unregisterSyncListener(mNflGameOracleSyncListener);
        // Schedule.unregisterGameSyncListener(gameSyncListener);
        // UserSyncManager.getInstance().unregisterSyncListener(mUserSyncListener);
    }

    public void updateScoreboard() {
        updateDisplayedGameState();
        updateDisplayedPredictions();
    }

    public void updateDisplayedGameState() {
        if (game.isLocked()) {
            lock.setVisibility(View.VISIBLE);
        }

        Log.d(TAG, "updateDisplayedGameState()");
        Log.d(TAG, String.valueOf(game.getQuarter()));
        Log.d(TAG, Util.formatQuarter(getResources(), game.getQuarter()));
        if (game.isFinal()) {
            mQuarter.setText("FINAL");
            mClock.setVisibility(View.GONE);
        } else {
            mQuarter.setText(Util.formatQuarter(getResources(), game.getQuarter()));
            mClock.setText(game.getClock());
        }

        mAwayAbbr.setText(game.getAwayTeam().getAbbr());
        mAwayName.setText(game.getAwayTeam().getName());
        mHomeAbbr.setText(game.getHomeTeam().getAbbr());
        mHomeName.setText(game.getHomeTeam().getName());

        mAwayScore.setText(String.valueOf(game.getAwayTeam().getScore()));
        mHomeScore.setText(String.valueOf(game.getHomeTeam().getScore()));

        notifyGameStateChanged();
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
        switch (prediction.winner()) {
            case Prediction.W_AWAY:
                return game.getAwayTeam();
            case Prediction.W_HOME:
                return game.getHomeTeam();
            default:
                return null;
        }
    }

    private void updateDisplayedPredictions() {
        if (game.isLocked() && !prediction.isComplete()) {
            mLowerScoreboard.setVisibility(View.GONE);
            return;
        }

        mAwayFlipCard.setScore(prediction.getAwayScore());
        mHomeFlipCard.setScore(prediction.getHomeScore());
        lowerMiddleContainer.getSpreadView().setSpread(prediction.getSpread(game));
        lowerMiddleContainer.getSpreadView().setProgress(prediction.getSpread(game));
        markPredictedWinner();
    }

    private void confirmPrediction() {
        synchronized (prediction) {
            if (mSelectedFlipCard == mAwayFlipCard)
                prediction.setAwayScore(mAwayFlipCard.getScore());
            else if (mSelectedFlipCard == mHomeFlipCard)
                prediction.setHomeScore(mHomeFlipCard.getScore());

            // Database.putPrediction(prediction);
            // TODO: Send prediction to server
        }

        markPredictedWinner();
        hideNumberPadFragment(!mSelectedTeam.isHome());
        mSelectedFlipCard = null;
        mSelectedTeam = null;
        mPredictSession = false;

        notifyPredictionChanged();
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
        int white = ContextCompat.getColor(this, android.R.color.white);
        int standardText = ContextCompat.getColor(this, R.color.standard_text);

        switch (prediction.winner()) {
            case Prediction.W_AWAY:
                if (mNumberPadFragment != null)
                    mNumberPadFragment.setBufferColor(game.getAwayTeam().getPrimaryColor());
                mAwayFlipCard.solidBackground(game.getAwayTeam().getPrimaryColor(), white);
                mHomeFlipCard.strokedBackground(white, white);
                changeScoreboardColor(game.getAwayTeam().getPrimaryColor());
                break;
            case Prediction.W_HOME:
                if (mNumberPadFragment != null)
                    mNumberPadFragment.setBufferColor(game.getHomeTeam().getPrimaryColor());
                mAwayFlipCard.strokedBackground(white, white);
                mHomeFlipCard.solidBackground(game.getHomeTeam().getPrimaryColor(), white);
                changeScoreboardColor(game.getHomeTeam().getPrimaryColor());
                break;
            default:
                if (mNumberPadFragment != null)
                    mNumberPadFragment.setBufferColor(standardText);
                mAwayFlipCard.strokedBackground(white, white);
                mHomeFlipCard.strokedBackground(white, white);
                changeScoreboardColor(standardText);
        }
    }

    private void changeScoreboardColor(int color) {
        scoreboardColor = color;

        mToolbar.setBackgroundColor(color);
        mUpperScoreboard.setBackgroundColor(color);
        mLowerScoreboard.setBackgroundColor(color);
        mTabs.setBackgroundColor(color);

        FriendPredictionFragment f = (FriendPredictionFragment) mPagerAdapter.getRegisteredFragment(GamePagerAdapter.FRIENDS_PAGE);
        if (f != null)
            f.setHeaderColor(color & 0xc0ffffff);
    }

    public int getScoreboardColor() {
        return scoreboardColor;
    }

    public NflGame getGame() {
        return game;
    }

    public void registerGameStateChangeListener(GameStateChangeListener listener) {
        gameStateChangeListeners.add(listener);
    }

    public void unregisterGameStateChangeListener(GameStateChangeListener listener) {
        gameStateChangeListeners.remove(listener);
    }

    public void notifyGameStateChanged() {
        for (GameStateChangeListener listener : gameStateChangeListeners)
            listener.onGameStateChanged(game);
    }

    public void notifyPredictionChanged() {
        for (GameStateChangeListener listener : gameStateChangeListeners)
            listener.onPredictionChanged(prediction);
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
