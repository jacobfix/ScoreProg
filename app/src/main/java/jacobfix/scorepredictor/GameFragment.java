package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import jacobfix.scorepredictor.task.RankPredictionsTask;
import jacobfix.scorepredictor.task.SortPredictionsTask;
import jacobfix.scorepredictor.task.SubmitPredictionTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.UserDetails;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Pair;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class GameFragment extends Fragment implements PredictionFragment.PredictionFragmentListener,
        NumberPadFragment.NumberPadFragmentListener {

    private static final String TAG = GameFragment.class.getSimpleName();

    private AtomicGame atom;
    private FullGame full;

    private Prediction prediction;

    // private Participants participants;

    private Map<String, Prediction> participants;

    private ProgressBar loadingCircle;

    private LinearLayout scoreboardContainer;

    private RelativeLayout upperScoreboardContainer;
    private RelativeLayout lowerScoreboardContainer;

    private UpperMiddleContainer upperMiddleContainer;
    private LowerMiddleContainer lowerMiddleContainer;

    private ImageView lock;

    private RelativeLayout awayTeamContainer;
    private RelativeLayout homeTeamContainer;

    private TextView awayAbbr;
    private TextView homeAbbr;
    private TextView awayName;
    private TextView homeName;

    private TextView awayScore;
    private TextView homeScore;

    private FrameLayout awayPredictionContainer;
    private FrameLayout homePredictionContainer;

    private PredictionView awayPrediction;
    private PredictionView homePrediction;

    private PredictionView activePrediction;
    private Team activeTeam;
    private boolean predictSession;

    private ViewPager pager;
    private GameFragmentPagerAdapter pagerAdapter;
    private TabLayout tabs;

    private int scoreboardColor;

    private NewGameActivity activity;
    private NumberPadFragment numberPadFragment;
    private PredictionFragment predictionFragment;

    private PredictionFragment.State predictionFragmentState = PredictionFragment.State.LOADING;

    private boolean uiInitialized;
    private boolean fullGameRetrieved;
    private boolean predictionRetrieved;

    private static final int AWAY_TAG = 0;
    private static final int HOME_TAG = 1;

    private static final int MAX_PREDICTION_DIGITS = 2;

    // TODO: AsyncCallbacks here?

    public static GameFragment newInstance(String gameId) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putString("gameId", gameId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (NewGameActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        final String gameId = getArguments().getString("gameId");

        this.atom = Schedule.getGame(gameId);

        PredictionProvider.getPrediction(LocalAccountManager.get().getId(), gameId, new AsyncCallback<Prediction>() {
            @Override
            public void onSuccess(Prediction result) {
                prediction = (result != null) ? result : new Prediction(LocalAccountManager.get().getId(), gameId);

                Log.d(TAG, atom.getId() + " prediction retrieved");
                predictionRetrieved = true;
                if (fullGameRetrieved && uiInitialized)
                    finishedInitializing();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

        GameProvider.getFullGame(atom, new AsyncCallback<FullGame>() {
            @Override
            public void onSuccess(FullGame result) {
                // TODO: If null?
                full = result;

                Log.d(TAG, atom.getId() + " full game retrieved");
                fullGameRetrieved = true;
                if (predictionRetrieved && uiInitialized)
                    finishedInitializing();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    private void finishedInitializing() {
        updateDisplayedGameState();
        updateDisplayedPredictions();
        showScoreboard();

        PredictionFragment fragment = getPredictionFragment();
        if (fragment != null)
            fragment.onGameStateChanged(full);

        showPredictionFragmentLoading();

        updateParticipants(prediction.getExposure(), new AsyncCallback<Map<String, Prediction>>() {
            @Override
            public void onSuccess(Map<String, Prediction> result) {
                GameFragment.this.participants = result;
                PredictionFragment fragment = getPredictionFragment();
                if (fragment != null) {
                    fragment.updateParticipants(participants);
                }
                showPredictionFragmentList();
            };

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
                // showPredictionFragmentError() ?
            }
        });
    }

    private void updateParticipants(int exposure, final AsyncCallback<Map<String, Prediction>> callback) {
        getPotentialParticipants(exposure, new AsyncCallback<Collection<String>>() {
            @Override
            public void onSuccess(Collection<String> potentialParticipants) {
                PredictionProvider.getPredictions(potentialParticipants, full.getId(), callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private void getPotentialParticipants(int exposure, AsyncCallback<Collection<String>> callback) {
        switch (exposure) {
            case Prediction.X_FRIENDS:
                callback.onSuccess(LocalAccountManager.get().getFriends().getConfirmed());
                break;

            case Prediction.X_INVITE:
                // TODO: getInvitees method
                break;

            default:
                throw new RuntimeException("Invalid value for prediction exposure");
        }
    }

    private void showScoreboard() {
        loadingCircle.setVisibility(View.GONE);
        scoreboardContainer.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        scoreboardContainer.setVisibility(View.INVISIBLE);
        loadingCircle.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        initializeViews(view);
        initializeTabs(view);

        setScoreboardColor(ColorUtil.STANDARD_TEXT);
        showLoading();

        Log.d(TAG, atom.getId() + " ui initialized");
        uiInitialized = true;
        if (predictionRetrieved && fullGameRetrieved)
            finishedInitializing();

        return view;
    }

    private void initializeViews(View container) {
        loadingCircle = ViewUtil.findById(container, R.id.loading_circle);
        scoreboardContainer = ViewUtil.findById(container, R.id.scoreboard_container);

        upperScoreboardContainer = ViewUtil.findById(container, R.id.upper_scoreboard_container);
        lowerScoreboardContainer = ViewUtil.findById(container, R.id.lower_scoreboard_container);

        upperMiddleContainer = ViewUtil.findById(container, R.id.upper_middle_container);
        lowerMiddleContainer = ViewUtil.findById(container, R.id.lower_middle_container);

        lock = ViewUtil.findById(container, R.id.lock);

        awayTeamContainer = ViewUtil.findById(container, R.id.away_block);
        homeTeamContainer = ViewUtil.findById(container, R.id.home_block);

        awayAbbr = ViewUtil.findById(container, R.id.away_abbr);
        homeAbbr = ViewUtil.findById(container, R.id.home_abbr);
        awayName = ViewUtil.findById(container, R.id.away_name);
        homeName = ViewUtil.findById(container, R.id.home_name);

        awayScore = ViewUtil.findById(container, R.id.away_score_actual);
        homeScore = ViewUtil.findById(container, R.id.home_score_actual);

        awayPredictionContainer = ViewUtil.findById(container, R.id.away_prediction_block);
        homePredictionContainer = ViewUtil.findById(container, R.id.home_prediction_block);

        awayPrediction = ViewUtil.findById(container, R.id.away_flip_card);
        homePrediction = ViewUtil.findById(container, R.id.home_flip_card);

        Typeface nameTypeface = FontHelper.getYantramanavBold(getContext());
        awayAbbr.setTypeface(nameTypeface);
        homeAbbr.setTypeface(nameTypeface);
        awayName.setTypeface(nameTypeface);
        homeName.setTypeface(nameTypeface);

        Typeface scoreTypeface = FontHelper.getYantramanavRegular(getContext());
        awayScore.setTypeface(scoreTypeface);
        homeScore.setTypeface(scoreTypeface);

        awayPrediction.setTypeface(scoreTypeface);
        homePrediction.setTypeface(scoreTypeface);

        awayPrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
        homePrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);

        View.OnClickListener onTeamClickedListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (full.isLocked())
                    return;

                PredictionView selected;
                Team selectedTeam;

                switch((int) view.getTag()) {
                    case AWAY_TAG:
                        selected = awayPrediction;
                        selectedTeam = full.getAwayTeam();
                        break;

                    case HOME_TAG:
                        selected = homePrediction;
                        selectedTeam = full.getHomeTeam();
                        break;

                    default:
                        throw new RuntimeException("Selected PredictionView had neither a HOME nor an AWAY tag");

                }

                if (activePrediction != null) {
                    if (selected == activePrediction)
                        return;
                    confirmPrediction();
                }

                activePrediction = selected;
                activeTeam = selectedTeam;

                showNumberPadFragment(activeTeam, scoreboardColor, !activeTeam.isHome());
            }
        };

        awayTeamContainer.setTag(AWAY_TAG);
        awayTeamContainer.setOnClickListener(onTeamClickedListener);

        homeTeamContainer.setTag(HOME_TAG);
        homeTeamContainer.setOnClickListener(onTeamClickedListener);

        awayPredictionContainer.setTag(AWAY_TAG);
        awayPredictionContainer.setOnClickListener(onTeamClickedListener);

        homePredictionContainer.setTag(HOME_TAG);
        homePredictionContainer.setOnClickListener(onTeamClickedListener);

        awayPrediction.setTag(AWAY_TAG);
        homePrediction.setTag(HOME_TAG);
    }

    private void initializeTabs(View container) {
        pager = ViewUtil.findById(container, R.id.pager);
        pagerAdapter = new GameFragmentPagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabs = ViewUtil.findById(container, R.id.tabs);
        tabs.setupWithViewPager(pager);
    }

    private void updateDisplayedGameState() {
        if (full.isLocked()) lock.setVisibility(View.VISIBLE);
        else                 lock.setVisibility(View.GONE);

        if (full.isPregame()) {
            /* Don't show 0-0 as the score prior to game time. */
            awayScore.setVisibility(View.GONE);
            homeScore.setVisibility(View.GONE);
            upperMiddleContainer.pregameDisplay(full);
        } else if (full.isFinal()) {
            awayScore.setVisibility(View.VISIBLE);
            homeScore.setVisibility(View.VISIBLE);
            upperMiddleContainer.finalDisplay(full);
        } else {
            awayScore.setVisibility(View.VISIBLE);
            homeScore.setVisibility(View.VISIBLE);
            upperMiddleContainer.inProgressDisplay(full);
        }

        awayAbbr.setText(full.getAwayTeam().getAbbr());
        homeAbbr.setText(full.getHomeTeam().getAbbr());
        awayName.setText(full.getAwayTeam().getName());
        homeName.setText(full.getHomeTeam().getName());

        awayScore.setText(String.valueOf(full.getAwayTeam().getScore()));
        homeScore.setText(String.valueOf(full.getHomeTeam().getScore()));

        // notifyGameStateChanged();
    }

    private void updateDisplayedPredictions() {
        if (full.isLocked()) {
            lowerMiddleContainer.showSpread();
            if (!prediction.isComplete()) lowerScoreboardContainer.setVisibility(View.GONE);
            else                          lowerScoreboardContainer.setVisibility(View.VISIBLE);
        } else {
            lowerScoreboardContainer.setVisibility(View.VISIBLE);
            if (!prediction.isComplete()) lowerMiddleContainer.showUnpredictedText();
            else                          lowerMiddleContainer.showPredictedText();
        }

        awayPrediction.setScore(prediction.getAwayScore());
        homePrediction.setScore(prediction.getHomeScore());

        lowerMiddleContainer.getSpreadView().setSpread(prediction.getSpread(full));
        lowerMiddleContainer.getSpreadView().setProgress(prediction.getSpread(full));

        markPredictedWinner();
    }

    private void confirmPrediction() {
        synchronized (prediction.getLock()) {
            int tag = (int) activePrediction.getTag();
            switch (tag) {
                case AWAY_TAG:
                    prediction.setAwayScore(awayPrediction.getScore());
                    break;

                case HOME_TAG:
                    prediction.setHomeScore((homePrediction.getScore()));
                    break;

                default:
                    throw new RuntimeException("PredictionView had neither a HOME nor an AWAY tag");
            }

            submitPrediction();
        }

        markPredictedWinner();
        hideNumberPadFragment(!activeTeam.isHome());
        activePrediction = null;
        activeTeam = null;
        predictSession = false;
    }

    private void submitPrediction() {
        prediction.setModified(true);
        prediction.setOnServer(false);

        lowerMiddleContainer.showLoading();

        new SubmitPredictionTask(LocalAccountManager.get().getId(), full.getId(), prediction.getAwayScore(), prediction.getHomeScore(), new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    // TODO: Show a snackbar or something
                    Log.e(TAG, task.getError().toString());
                    return;
                }

                synchronized (prediction.getLock()) {
                    prediction.setOnServer(true);
                    if (!prediction.isComplete()) lowerMiddleContainer.showUnpredictedText();
                    else                          lowerMiddleContainer.showPredictedText();
                }

            }
        }).start();
    }

    private void markPredictedWinner() {
        switch (prediction.winner()) {
            case Prediction.W_AWAY:
                if (numberPadFragment != null)
                    numberPadFragment.setBufferColor(full.getAwayTeam().getPrimaryColor());

                awayPrediction.solidBackground(full.getAwayTeam().getPrimaryColor(), ColorUtil.WHITE);
                homePrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);

                setScoreboardColor(full.getAwayTeam().getPrimaryColor());
                break;

            case Prediction.W_HOME:
                if (numberPadFragment != null)
                    numberPadFragment.setBufferColor(full.getHomeTeam().getPrimaryColor());

                awayPrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
                homePrediction.solidBackground(full.getHomeTeam().getPrimaryColor(), ColorUtil.WHITE);

                setScoreboardColor(full.getHomeTeam().getPrimaryColor());
                break;

            default:
                if (numberPadFragment != null)
                    numberPadFragment.setBufferColor(ColorUtil.STANDARD_TEXT);

                awayPrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
                homePrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);

                setScoreboardColor(ColorUtil.STANDARD_TEXT);
        }
    }

    public void setScoreboardColor(int color) {
        // TODO: put the scoreboard color change method in the activity?
        scoreboardColor = color;
        // activity.setStatusBarColor(color, this);
        activity.setToolbarColor(color, this);

        upperScoreboardContainer.setBackgroundColor(color);
        lowerScoreboardContainer.setBackgroundColor(color);
        tabs.setBackgroundColor(color);

        if (predictionFragment != null)
            predictionFragment.setHeaderColor(color & 0xc0ffffff);
    }

    private void showNumberPadFragment(Team team, int bufferColor, boolean fromLeft) {
        numberPadFragment = NumberPadFragment.newInstance(team.getLocale() + " " + team.getName(), team.getPrimaryColor(), bufferColor);
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (fromLeft) fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else          fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

        fragmentTransaction.add(R.id.container, numberPadFragment);
        fragmentTransaction.commit();
    }

    private void hideNumberPadFragment(boolean fromLeft) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (fromLeft) fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else          fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

        fragmentTransaction.remove(numberPadFragment);
        fragmentTransaction.commit();

        numberPadFragment = null;
    }

    @Override
    public PredictionFragment.State getPredictionFragmentState() {
        return predictionFragmentState;
    }

    @Override
    public void showPredictionFragmentLoading() {
        predictionFragmentState = PredictionFragment.State.LOADING;
        PredictionFragment fragment = getPredictionFragment();
        if (fragment != null)
            fragment.showLoading();
    }

    @Override
    public void showPredictionFragmentList() {
        predictionFragmentState = PredictionFragment.State.LIST;
        PredictionFragment fragment = getPredictionFragment();
        if (fragment != null)
            fragment.showList();
    }

    @Override
    public Map<String, Prediction> getParticipants() {
        return participants;
    }

    private PredictionFragment getPredictionFragment() {
        return (PredictionFragment) pagerAdapter.getRegisteredFragment(GameFragmentPagerAdapter.FRIENDS_PAGE);
    }

    @Override
    public int getScoreboardColor() {
        return scoreboardColor;
    }

    @Override
    public FullGame getGame() {
        return full;
    }

    @Override
    public Prediction getPrediction() {
        return prediction;
    }

    private void clearPrediction() {
        activePrediction.clear();
    }

    private void addDigitToPrediction(String digit) {
        Log.d(TAG, "Adding digit: " + digit);
        if (!predictSession) {
            clearPrediction();
            predictSession = true;
        }
        if (activePrediction.getTextView().length() < MAX_PREDICTION_DIGITS)
            activePrediction.append(digit);
    }

    @Override
    public void keyPressed(NumberPadFragment.Key k) {
        Log.d(TAG, "Key pressed");
        if (activePrediction == null)
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
