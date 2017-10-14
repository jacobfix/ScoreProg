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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.sync.PredictionProvider;
import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SortPredictionsTask;
import jacobfix.scorepredictor.task.SubmitPredictionTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.users.UserDetails;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class GameActivity extends AppCompatActivity implements NumberPadFragment.NumberPadFragmentListener {

    private static final String TAG = GameActivity.class.getSimpleName();

    private AtomicGame atom;
    private FullGame full;

    private NflGame game;
    private Prediction prediction;

    private Collection<String> participants;
    private Map<String, UserDetails> userDetails;
    private ArrayList<Prediction> sortedPredictions;

    private PredictionFragment predictionFragment;

    private ArrayList<User> mRankedParticipants;

    private ProgressBar loadingSymbol;
    private Toolbar mToolbar;
    private TextView weekTitle;

    private LinearLayout scoreboard;
    private RelativeLayout mUpperScoreboard;
    private RelativeLayout mLowerScoreboard;

    // private LinearLayout mUpperMiddleContainer;
    private SpreadView spread;

    private UpperMiddleContainer upperMiddleContainer;
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

    private Team selectedTeam;

    private PredictionView mSelectedFlipCard;
    private NflTeam mSelectedTeam;
    private boolean mPredictSession;

    private TabLayout mTabs;
    private ViewPager mPager;
    private GameFragmentPagerAdapter mPagerAdapter;

    private int scoreboardColor;

    private static final Object loadLock = new Object();

    private boolean uiInitialized;
    private boolean fullGameRetrieved;
    private boolean predictionRetrieved;

    private BroadcastReceiver lockBroadcastReceiver;

    private AsyncCallback userDetailsSyncCallback;
    private AsyncCallback predictionSyncCallback;

    private HashSet<GameStateChangeListener> gameStateChangeListeners = new HashSet<>();

    private AsyncCallback<Map<String, NflGame>> gameSyncListener;
    private AsyncCallback<User[]> detailsSyncListener;
    private AsyncCallback<OriginalPredictions[]> predictionsSyncListener;

    private FriendPredictionFragment mFriendPredictionFragment;
    private NumberPadFragment mNumberPadFragment;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final String GAME_ID_EXTRA = "game_id";
    public static final String ACTION_ANNOUNCE_GAME_LOCKED = "lock";

    private static final int NEITHER_TAG = 0;
    private static final int AWAY_TAG = 1;
    private static final int HOME_TAG = 2;

    public static final int X_FRIENDS = 1;
    public static final int X_INVITE = 2;

    private static final int MAX_PREDICTION_DIGITS = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_material_triple_new);

        /* This activity must be passed a game ID on creation. */
        final String gameId = getIntent().getStringExtra("game");
        if (gameId == null)
            Log.wtf(TAG, "GameActivity was started without a game ID");

        this.atom = Schedule.getGame(gameId);

        Log.d(TAG, "Getting prediction");
        PredictionProvider.getPrediction(LocalAccountManager.get().getId(), gameId, new AsyncCallback<Prediction>() {
            @Override
            public void onSuccess(Prediction result) {
                Log.d(TAG, "Got prediction");
                prediction = (result != null) ? result : new Prediction(LocalAccountManager.get().getId(), gameId);

                predictionRetrieved = true;
                if (fullGameRetrieved && uiInitialized)
                    finishInitializing();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

        GameProvider.getFullGame(atom, new AsyncCallback<FullGame>() {
            @Override
            public void onSuccess(FullGame result) {
                Log.d(TAG, "Got full game");
                full = result;

                fullGameRetrieved = true;
                if (predictionRetrieved && uiInitialized)
                    finishInitializing();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

        lockBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String gameId = intent.getStringExtra("game_id");
                if (gameId.equals(atom.getId())) {
                    if (mNumberPadFragment != null)
                        hideNumberPadFragment(!mSelectedTeam.isHome());

                    updateDisplayedGameState();
                    updateDisplayedPredictions();
                }
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(lockBroadcastReceiver, new IntentFilter(ACTION_ANNOUNCE_GAME_LOCKED));

        /* Maybe try putting this in a task. */
        Log.d(TAG, "Starting UI init");
        initializeToolbar();
        initializeViews();
        initializeTabsAndPager();
        initializeCallbacks();

        GameProvider.setGameToSync(atom);
        PredictionProvider.addPredictionToSync(LocalAccountManager.get().getId(), atom.getId());

        // PredictionProvider.setGameIdToSync(atom.getId());

        /*
        UserProvider.setUserIdsToSync(participants);

        PredictionProvider.setUserIdsToSync(participants);
        */

        changeScoreboardColor(ContextCompat.getColor(GameActivity.this, R.color.standard_text));
        weekTitle.setText(Util.getWeekTitle(atom.getWeekType(), atom.getWeek()));

        Log.d(TAG, "Finished initializing UI");
        uiInitialized = true;
        if (predictionRetrieved && fullGameRetrieved)
            finishInitializing();
    }

    public void finishInitializing() {
        updateDisplayedGameState();
        updateDisplayedPredictions();
        showScoreboard();

        Log.d(TAG, "Updating participants for the first time");
        updateParticipants(prediction.getExposure(), new AsyncCallback<Collection<String>>() {
            @Override
            public void onSuccess(Collection<String> result) {
                Log.d(TAG, "About to update sorted predictions after updating participants for the first time");
                updateSortedPredictions();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

        UserProvider.registerSyncCallback(userDetailsSyncCallback);
        PredictionProvider.registerSyncCallback(predictionSyncCallback);
        Log.d(TAG, "Finished UI init");
    }

    private void showScoreboard() {
        loadingSymbol.setVisibility(View.GONE);
        scoreboard.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        scoreboard.setVisibility(View.INVISIBLE);
        loadingSymbol.setVisibility(View.VISIBLE);
    }

    private void updateParticipants(int exposure, final AsyncCallback<Collection<String>> callback) {
        switch (exposure) {
            case X_FRIENDS:
                participants = LocalAccountManager.get().getFriendIds();
                participants.add(LocalAccountManager.get().getId());
                PredictionProvider.setPredictionsToSync(participants, Collections.singletonList(atom.getId()));
                UserProvider.setUserIdsToSync(participants);

                if (callback != null)
                    callback.onSuccess(participants);
                break;

            case X_INVITE:
                UserProvider.getInvitees(atom.getId(), new AsyncCallback<ArrayList<String>>() {
                    @Override
                    public void onSuccess(ArrayList<String> result) {
                        participants = result;
                        participants.add(LocalAccountManager.get().getId());
                        PredictionProvider.setPredictionsToSync(participants, Collections.singletonList(atom.getId()));
                        UserProvider.setUserIdsToSync(participants);

                        if (callback != null)
                            callback.onSuccess(participants);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, e.toString());
                        if (callback != null)
                            callback.onFailure(e);
                    }
                });
                break;

            default:
                throw new RuntimeException();
        }
    }

    private void initializeToolbar() {
        mToolbar = ViewUtil.findById(this, R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        weekTitle = ViewUtil.findById(this, R.id.title);
        weekTitle.setTypeface(FontHelper.getYantramanavRegular(this));
        // weekTabs = ViewUtil.findById(this, R.id.week_tabs);
    }

    private void initializeViews() {
        loadingSymbol = ViewUtil.findById(this, R.id.loading_circle);

        // mScoreboard = ViewUtil.findById(this, R.id.scoreboard);
        scoreboard = ViewUtil.findById(this, R.id.scoreboard);
        mUpperScoreboard = ViewUtil.findById(this, R.id.upper_scoreboard_container);
        mLowerScoreboard = ViewUtil.findById(this, R.id.lower_scoreboard_container);

        upperMiddleContainer = ViewUtil.findById(this, R.id.upper_middle_container);
        // upperMiddleContainer.pregameDisplay();
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
                if (full.isLocked())
                    return;

                Log.d(TAG, "DFP " + prediction.toString());
                Log.d(TAG, "DFP AWAY SCORE: " + prediction.getAwayScore());
                Log.d(TAG, "DFP HOME SCORE: " + prediction.getHomeScore());

                Log.d(TAG, "CLICKED");

                PredictionView touched;
                Team team;
                switch ((int) view.getTag()) {
                    case AWAY_TAG:
                        touched = mAwayFlipCard;
                        team = full.getAwayTeam();
                        // team = game.getAwayTeam();
                        break;
                    case HOME_TAG:
                        touched = mHomeFlipCard;
                        team = full.getHomeTeam();
                        // team = game.getHomeTeam();
                        break;
                    default:
                        throw new RuntimeException("Selected PredictionView had neither a HOME nor an AWAY tag");
                }

                if (mSelectedFlipCard != null) {
                    /* If there is an already selected flip card... */
                    if (touched == mSelectedFlipCard) {
                        return;
                    }
                    confirmPrediction();
                }
                mSelectedFlipCard = touched;
                selectedTeam = team;
                // showNumberPadFragment(mSelectedTeam, (int) view.getTag() == AWAY_TAG);
                // NflTeam predictedWinner = getPredictedWinner();

                Team predictedWinner = getPredictedWinner();
                int bufferColor = (predictedWinner == null) ? ContextCompat.getColor(GameActivity.this, R.color.standard_text) : predictedWinner.getPrimaryColor();

                showNumberPadFragment(team.getLocale() + " " + team.getName(), team.getPrimaryColor(), bufferColor, !team.isHome());
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
        mPagerAdapter = new GameFragmentPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mTabs = ViewUtil.findById(this, R.id.tabs);
        mTabs.setupWithViewPager(mPager);
        mTabs.setSelectedTabIndicatorColor(Color.WHITE);

        // mFriendPredictionFragment = (FriendPredictionFragment) mPagerAdapter.getItem(mPagerAdapter.FRIENDS_PAGE);
    }

    private void updateSortedPredictions() {
        /*
        PredictionProvider.getPredictions(participants, Collections.singletonList(atom.getId()), true, new AsyncCallback<Predictions>() {
            @Override
            public void onSuccess(Predictions result) {
                new SortPredictionsTask(result.getAll(), new PredictionComparator(atom), new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(BaseTask task) {
                        if (task.errorOccurred()) {
                            Log.e(TAG, task.getError().toString());
                            onFailure(task.getError());
                        }
                        Log.d(TAG, "asyp ABOUT TO SET SORTED PREDICTIONS");
                        sortedPredictions = (ArrayList<Prediction>) task.getResult();

                        PredictionFragment fragment = getPredictionFragment();
                        if (fragment != null) {
                            // fragment.setSortedPredictions(sortedPredictions);
                        }
                    }
                }).start();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
        */
    }

    private void initializeCallbacks() {
        userDetailsSyncCallback = new AsyncCallback<ArrayList<UserDetails>>() {
            @Override
            public void onSuccess(ArrayList<UserDetails> result) {
                /* This activity doesn't need to do anything when the UserDetails sync completes. */
                Log.d(TAG, "UserDetails sync finished");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        };

        predictionSyncCallback = new AsyncCallback<ArrayList<Prediction>>() {
            @Override
            public void onSuccess(ArrayList<Prediction> result) {
                /*
                updateParticipants(prediction.getExposure(), new AsyncCallback<Collection<String>>() {
                    @Override
                    public void onSuccess(Collection<String> result) {
                        updateDisplayedPredictions();
                        updateSortedPredictions();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
                */
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
            /* If the fragment is null, then it will not be updated from the
               activity, but will retrieve the sorted predictions from the
               activity when it attaches.

               fragment attaches before activity has sorted predictions
                  - fragment shows loading, will be changed to list when
                    updated by activity

               fragment attaches after activity has sorted predictions
                  - fragment gets sorted predictions from activity
               */
        };
    }

    public void registerPredictionFragment(PredictionFragment fragment) {
        Log.d(TAG, "asyp Registering PredictionFragment");
        predictionFragment = fragment;
    }

    public void unregisterPredictionFragment(PredictionFragment fragment) {
        Log.d(TAG, "asyp Unregistering PredictionFragment");
        if (predictionFragment == fragment) {
            Log.d(TAG, "asyp Really unregistering PredictionFragment");
            predictionFragment = null;
        }
    }

    private PredictionFragment getPredictionFragment() {
        // return (PredictionFragment) mPagerAdapter.getRegisteredFragment(GameFragmentPagerAdapter.FRIENDS_PAGE);
        return predictionFragment;
    }

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
                        FriendPredictionFragment fragment = (FriendPredictionFragment) mPagerAdapter.getRegisteredFragment(GameFragmentPagerAdapter.FRIENDS_PAGE);
                        fragment.setFriends(mRankedParticipants);
                    }
                }).start();

                /* ArrayList<User> asList = new ArrayList<>();
                for (User user : participants) {
                    asList.add(user);
                }
                mRankedParticipants = asList;

                FriendPredictionFragment fragment = (FriendPredictionFragment) mPagerAdapter.getRegisteredFragment(GameFragmentPagerAdapter.FRIENDS_PAGE);
                fragment.setFriends(asList); */

        /*        // TODO: We should be able to reset the users to sync to a much smaller set, i.e., only the users playing this game
            }

            @Override
            public void onSyncError() {
                Log.d(TAG, "User sync error");
            }
        }; */
    // }

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        UserProvider.unregisterSyncCallback(userDetailsSyncCallback);
        PredictionProvider.unregisterSyncCallback(predictionSyncCallback);
    }

    public void updateScoreboard() {
        updateDisplayedGameState();
        updateDisplayedPredictions();
    }

    public void updateDisplayedGameState() {
        if (full.isLocked()) {
            lock.setVisibility(View.VISIBLE);
        } else {
            lock.setVisibility(View.GONE);
        }

        if (full.isPregame()) {
            mAwayScore.setVisibility(View.GONE);
            mHomeScore.setVisibility(View.GONE);
            upperMiddleContainer.pregameDisplay(full);
        } else if (full.isFinal()) {
            mAwayScore.setVisibility(View.VISIBLE);
            mHomeScore.setVisibility(View.VISIBLE);
            upperMiddleContainer.finalDisplay(full);
        } else {
            mAwayScore.setVisibility(View.VISIBLE);
            mHomeScore.setVisibility(View.VISIBLE);
            // upperMiddleContainer.setQuarter(full.getQuarter());
            // upperMiddleContainer.setClock(full.getClock());
            upperMiddleContainer.inProgressDisplay(full);
        }

        mAwayAbbr.setText(full.getAwayTeam().getAbbr());
        mAwayName.setText(full.getAwayTeam().getName());
        mHomeAbbr.setText(full.getHomeTeam().getAbbr());
        mHomeName.setText(full.getHomeTeam().getName());

        Log.d(TAG, "Game is pregame: " + full.isPregame());
        Log.d(TAG, "Displaying away score: " + full.getAwayTeam().getScore());
        Log.d(TAG, "Displaying home score: " + full.getHomeTeam().getScore());
        mAwayScore.setText(String.valueOf(full.getAwayTeam().getScore()));
        mHomeScore.setText(String.valueOf(full.getHomeTeam().getScore()));

        notifyGameStateChanged();
    }

    public Map<String, UserDetails> getUserDetails() {
        return userDetails;
    }

    public ArrayList<Prediction> getSortedPredictions() {
        return sortedPredictions;
    }

    /*
    public void updateDisplayedGameState() {
        if (game.isLocked()) {
            lock.setVisibility(View.VISIBLE);
        }

        if (game.isPregame()) {
            mAwayScore.setVisibility(View.GONE);
            mHomeScore.setVisibility(View.GONE);
            upperMiddleContainer.setDate(Util.getDateStringFromId(game.getGameId(), true));
            upperMiddleContainer.setStartTime(game.getStartTimeDisplay(), game.getMeridiem());
            upperMiddleContainer.pregameDisplay();
        } else if (game.isFinal()) {
            mAwayScore.setVisibility(View.VISIBLE);
            mHomeScore.setVisibility(View.VISIBLE);
            upperMiddleContainer.finalDisplay();
        } else {
            mAwayScore.setVisibility(View.VISIBLE);
            mHomeScore.setVisibility(View.VISIBLE);
            upperMiddleContainer.setQuarter(Util.formatQuarter(getResources(), game.getQuarter()));
            upperMiddleContainer.setClock(game.getClock());
            upperMiddleContainer.inProgressDisplay();
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
    */

    private void showNumberPadFragment(String title, int color, int bufferColor, boolean fromLeft) {
        mNumberPadFragment = NumberPadFragment.newInstance(title, color, bufferColor);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (fromLeft)
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

        fragmentTransaction.add(R.id.container, mNumberPadFragment);
        fragmentTransaction.commit();
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

    /*
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
    */

    private Team getPredictedWinner() {
        switch (prediction.winner()) {
            case Prediction.W_AWAY:
                return full.getAwayTeam();
            case Prediction.W_HOME:
                return full.getHomeTeam();
            default:
                return null;
        }
    }

    private void updateDisplayedPredictions() {
        if (full.isLocked()) {
            lowerMiddleContainer.showSpread();
            if (!prediction.isComplete()) mLowerScoreboard.setVisibility(View.GONE);
            else                          mLowerScoreboard.setVisibility(View.VISIBLE);
        } else {
            if (!prediction.isComplete()) lowerMiddleContainer.showUnpredictedText();
            else                          lowerMiddleContainer.showPredictedText();
        }

        Log.d(TAG, "DFP " + prediction.toString());
        Log.d(TAG, "DFP Prediction update, Away: " + prediction.getAwayScore() + ", Home: " + prediction.getHomeScore());
        mAwayFlipCard.setScore(prediction.getAwayScore());
        mHomeFlipCard.setScore(prediction.getHomeScore());
        // lowerMiddleContainer.getSpreadView().setSpread(prediction.getSpread(atom));
        // lowerMiddleContainer.getSpreadView().setProgress(prediction.getSpread(atom));
        markPredictedWinner();
    }

    private void confirmPrediction() {
        synchronized (prediction) {
            if (mSelectedFlipCard == mAwayFlipCard)
                prediction.setAwayScore(mAwayFlipCard.getScore());
            else if (mSelectedFlipCard == mHomeFlipCard)
                prediction.setHomeScore(mHomeFlipCard.getScore());

            prediction.setModified(true);
            prediction.setOnServer(false);

            new SubmitPredictionTask(LocalAccountManager.get().getId(), full.getId(), prediction.getAwayScore(), prediction.getHomeScore(), new TaskFinishedListener() {
                @Override
                public void onTaskFinished(BaseTask task) {
                    synchronized (prediction) {
                        if (task.errorOccurred()) {
                            Log.e(TAG, task.getError().toString());
                            return;
                        }
                        prediction.setOnServer(true);
                    }
                }
            }).start();
        }

        markPredictedWinner();
        hideNumberPadFragment(!selectedTeam.isHome());
        mSelectedFlipCard = null;
        // mSelectedTeam = null;
        selectedTeam = null;
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
                if (mNumberPadFragment != null) {
                    // mNumberPadFragment.setBufferColor(game.getAwayTeam().getPrimaryColor());
                    mNumberPadFragment.setBufferColor(full.getAwayTeam().getPrimaryColor());
                }
                mAwayFlipCard.solidBackground(full.getAwayTeam().getPrimaryColor(), white);
                mHomeFlipCard.strokedBackground(white, white);
                changeScoreboardColor(full.getAwayTeam().getPrimaryColor());
                break;
            case Prediction.W_HOME:
                if (mNumberPadFragment != null)
                    mNumberPadFragment.setBufferColor(full.getHomeTeam().getPrimaryColor());
                mAwayFlipCard.strokedBackground(white, white);
                mHomeFlipCard.solidBackground(full.getHomeTeam().getPrimaryColor(), white);
                changeScoreboardColor(full.getHomeTeam().getPrimaryColor());
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

        PredictionFragment fragment = getPredictionFragment();
        if (fragment != null)
            fragment.setHeaderColor(color & 0xc0ffffff);

        Util.changeStatusBarColor(this, color);

        /*
        FriendPredictionFragment f = (FriendPredictionFragment) mPagerAdapter.getRegisteredFragment(GameFragmentPagerAdapter.FRIENDS_PAGE);
        if (f != null)
            f.setHeaderColor(color & 0xc0ffffff);
            */

    }

    public int getScoreboardColor() {
        return scoreboardColor;
    }

    public AtomicGame getAtomicGame() { return atom; }
    public FullGame getFullGame() {
        return full;
    }

    public void registerGameStateChangeListener(GameStateChangeListener listener) {
        gameStateChangeListeners.add(listener);
    }

    public void unregisterGameStateChangeListener(GameStateChangeListener listener) {
        gameStateChangeListeners.remove(listener);
    }

    public void notifyGameStateChanged() {
        // for (GameStateChangeListener listener : gameStateChangeListeners)
            // listener.onGameStateChanged(atom);
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
