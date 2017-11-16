package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.Map;

import jacobfix.scorepredictor.components.PredictionView;
import jacobfix.scorepredictor.components.ScoreboardGameStateContainer;
import jacobfix.scorepredictor.components.ScoreboardPredictionStatusContainer;
import jacobfix.scorepredictor.components.ScoreboardView;
import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.sync.PredictionProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.SubmitPredictionTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class GameFragment extends Fragment implements PredictionFragment.PredictionFragmentListener,
        NumberPadFragment.NumberPadFragmentListener {

    private static final String TAG = GameFragment.class.getSimpleName();

    private FullGame game;

    private Prediction prediction;

    // private Participants participants;

    private Map<String, Prediction> participants;

    private Team selectedPredictionTeam;
    private boolean predictionsModified;

    private boolean inPredictSession;

    // private PredictionView activePrediction;
    // private OriginalTeam activeTeam;
    // private boolean predictSession;

    private ScoreboardView scoreboard;

    private ViewPager pager;
    private GameFragmentPagerAdapter pagerAdapter;
    private TabLayout tabs;

    private int scoreboardColor;

    private GameActivity activity;
    private NumberPadFragment numberPadFragment;
    private PredictionFragment predictionFragment;

    private PredictionFragment.State predictionFragmentState = PredictionFragment.State.LOADING;

    private boolean uiInitialized;
    private boolean fullGameRetrieved;
    private boolean predictionRetrieved;

    private State state;

    private static final int AWAY_TAG = 0;
    private static final int HOME_TAG = 1;

    private static final int MAX_PREDICTION_DIGITS = 2;

    // TODO: AsyncCallbacks here?

    public static GameFragment newInstance(String gameId) {
        Log.d(TAG, "Creating a new instance of GameFragment");
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putString("gameId", gameId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (GameActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        final String gameId = getArguments().getString("gameId");

        Game scheduledGame = Schedule.getGame(gameId);

        PredictionProvider.getPrediction(LocalAccountManager.get().getId(), gameId, new AsyncCallback<Prediction>() {
            @Override
            public void onSuccess(@Nullable Prediction result) {
                /* Result can be null, indicating that prediction for this game from this user does
                   not yet exist. */
                prediction = (result != null) ? result
                        : new Prediction(LocalAccountManager.get().getId(), gameId);

                Log.d(TAG, "Prediction retrieved");
                predictionRetrieved = true;
                if (fullGameRetrieved && uiInitialized)
                    finishedInitializing();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

        GameProvider.getFullGame(scheduledGame, new AsyncCallback<FullGame>() {
            @Override
            public void onSuccess(@NonNull FullGame result) {
                /* Result will not be null. A FullGame corresponding the provided Game will always
                   be returned by GameProvider, regardless of the game's state. */
                GameFragment.this.game = result;

                Log.d(TAG, "Full game retrieved");
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
        // scoreboard.updateGameState(game);
        // scoreboard.updatePrediction(prediction);
        // scoreboard.show();

        // updateDisplayedGameState();
        // updateDisplayedPredictions();
        // showScoreboard(true);

        updateScoreboardDisplay();
        scoreboard.show();

//        scoreboard.updateGameState(game);
//        scoreboard.updatePredictionState(game, prediction);
//        scoreboard.show();

        PredictionFragment fragment = getPredictionFragment();
        if (fragment != null) {
            fragment.onGameStateChanged(game);
            fragment.setHeaderColor(getScoreboardColor());
        }

        showPredictionFragmentLoading();

        updateParticipants(prediction.getExposure(), new AsyncCallback<Map<String, Prediction>>() {
            @Override
            public void onSuccess(Map<String, Prediction> result) {
                GameFragment.this.participants = result;
                Log.d(TAG, "Got the game's participants. Size: " + result.size());
                PredictionFragment fragment = getPredictionFragment();
                if (fragment != null)
                    fragment.updateParticipants(participants);
                
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
                // TODO: Make PredictionProvider not return a Prediction if the prediction does not exist
                PredictionProvider.getPredictions(potentialParticipants, game.getId(), callback);
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

    /*
    private void showScoreboard(boolean animate) {
        // We want to animate this
        if (animate) {

        }
        scoreboardContainer.setVisibility(View.VISIBLE);
        loadingIcon.setVisibility(View.GONE);
        state = State.SCOREBOARD;
    }
    */

    /*
    private void showLoading() {
        scoreboardContainer.setVisibility(View.INVISIBLE);
        loadingIcon.setVisibility(View.VISIBLE);
    }
    */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // View view = inflater.inflate(R.layout.fragment_game, container, false);
        View view = inflater.inflate(R.layout.fragment_game_new, container, false);
        scoreboard = ViewUtil.findById(view, R.id.scoreboard);

        scoreboard.setTeamSelectedListener(new ScoreboardView.TeamSelectedListener() {
            @Override
            public void onTeamSelected(Team teamSelected) {
                if (game.isLocked())
                    return;

                if (GameFragment.this.selectedPredictionTeam != null) {
                    if (teamSelected == GameFragment.this.selectedPredictionTeam)
                        return;
                    confirmPrediction();
                }

                GameFragment.this.selectedPredictionTeam = teamSelected;
                String teamName;
                int teamColor;
                switch (teamSelected) {
                    case AWAY:
                        synchronized (game.acquireLock()) {
                            teamName = game.getAwayLocale() + " " + game.getAwayName();
                            teamColor = game.getAwayColor();
                        }
                        break;

                    case HOME:
                        synchronized (game.acquireLock()) {
                            teamName = game.getHomeLocale() + " " + game.getHomeName();
                            teamColor = game.getHomeColor();
                        }
                        break;

                    default:
                        throw new RuntimeException();
                }

                showNumberPadFragment(teamName, teamColor, teamSelected == Team.AWAY);
                // showNumberPadFragment();
            }
        });

        pager = ViewUtil.findById(view, R.id.pager);
        pagerAdapter = new GameFragmentPagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);

        tabs = ViewUtil.findById(view, R.id.tabs);
        tabs.setupWithViewPager(pager);

        setScoreboardColor(ScoreboardView.DEFAULT_COLOR);
        scoreboard.loading();

        Log.d(TAG, "UI initialized");
        uiInitialized = true;
        if (predictionRetrieved && fullGameRetrieved)
            finishedInitializing();

        return view;
    }

    private void confirmPrediction() {
        /* When a prediction is confirmed, we update the Prediction object with the values entered
           in each PredictionView on the scoreboard. confirmPrediction() is called each time a
           single PredictionView has been updated. */
        // TODO: Have a way of checking that the predictions have changed, so that we don't unnecessarily send predictions to the server
        // if (!predictionsModified)
        //    return;

        int predictedWinner;
        synchronized (prediction) {
            switch (selectedPredictionTeam) {
                case AWAY:
                    prediction.setAwayScore(scoreboard.getAwayPredictedScore());
                    break;

                case HOME:
                    prediction.setHomeScore(scoreboard.getHomePredictedScore());
                    break;
            }
            predictedWinner = prediction.winner();
        }
        submitPrediction();
        highlightPredictedWinner(predictedWinner);

        hideNumberPadFragment(selectedPredictionTeam == Team.AWAY);
        selectedPredictionTeam = null;
        inPredictSession = false;
    }

    private void clearPrediction() {
        synchronized (prediction) {
            switch (selectedPredictionTeam) {
                case AWAY:
                    prediction.setAwayScore(Prediction.NULL);
                    scoreboard.clearAwayPrediction();
                    break;

                case HOME:
                    prediction.setHomeScore(Prediction.NULL);
                    scoreboard.clearHomePrediction();
                    break;
            }
        }
    }

    private void highlightPredictedWinner(int predictedWinner) {
        int color;
        switch (predictedWinner) {
            case Prediction.W_NONE:
                color = ScoreboardView.DEFAULT_COLOR;
                scoreboard.highlightNeitherPrediction();
                break;

            case Prediction.W_AWAY:
                synchronized (game.acquireLock()) {
                    color = game.getAwayColor();
                }
                scoreboard.highlightAwayPrediction(color);
                break;

            case Prediction.W_HOME:
                synchronized (game.acquireLock()) {
                    color = game.getHomeColor();
                }
                scoreboard.highlightHomePrediction(color);
                break;

            case Prediction.W_TIE:
                color = ScoreboardView.DEFAULT_COLOR;
                scoreboard.highlightNeitherPrediction();
                break;

            default:
                throw new RuntimeException();
        }
        setScoreboardColor(color);
    }

    private void updateScoreboardDisplay() {
        boolean locked;
        int awayScore, homeScore;
        int awayColor, homeColor;
        Game.State gameState;
        synchronized (game.acquireLock()) {
            locked = game.isLocked();

            if (locked) scoreboard.showLock();
            else        scoreboard.hideLock();

            scoreboard.setAwayName(game.getAwayAbbr(), game.getAwayName());
            scoreboard.setHomeName(game.getHomeAbbr(), game.getHomeName());

            awayScore = game.getAwayScore();
            homeScore = game.getHomeScore();
            scoreboard.setAwayScore(awayScore);
            scoreboard.setHomeScore(homeScore);

            awayColor = game.getAwayColor();
            homeColor = game.getHomeColor();

            if (game.isPregame()) {
                scoreboard.hideScores();
                scoreboard.pregameDisplay(game.getStartTime());
                gameState = Game.State.PREGAME;
            } else if (game.isFinal()) {
                scoreboard.showScores();
                scoreboard.finalDisplay();
                gameState = Game.State.FINAL;
            } else {
                scoreboard.showScores();
                scoreboard.inProgressDisplay(game.getClock(), game.getQuarter());
                gameState = Game.State.IN_PROGRESS;
            }
        }

        int predictedWinner;
        synchronized (prediction.acquireLock()) {
            if (locked) {
                if (!prediction.isComplete()) {
                    /* Game is locked and a prediction was not made. Don't show the incomplete
                       predictions; hide the lower half of the scoreboard. */
                    scoreboard.hidePredictions();
                } else {
                    /* Game is locked and a full prediction was made. Show the prediction. */
                    scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.SPREAD);
                    scoreboard.showPredictions();
                }
            } else {
                /* Game is not locked, so a prediction can still be made. Show the blank predictions
                   while it is still possible for them to be set. */
                scoreboard.showPredictions();
                if (!prediction.isComplete()) {
                    scoreboard.setPredictionState(ScoreboardPredictionStatusContainer
                            .State.UNPREDICTED);
                } else {
                    scoreboard.setPredictionState(ScoreboardPredictionStatusContainer
                            .State.PREDICTED);
                }
            }

            scoreboard.setAwayPrediction(prediction.getAwayScore());
            scoreboard.setHomePrediction(prediction.getHomeScore());

            int spread = Prediction.computeSpread(prediction.getAwayScore(),
                    prediction.getHomeScore(), awayScore, homeScore);
            scoreboard.setPredictionSpread(spread);

            predictedWinner = prediction.winner();
        }

        Log.d(TAG, "Predicted winner: " + predictedWinner);
        highlightPredictedWinner(predictedWinner);
    }

    private void submitPrediction() {
        Prediction.disableUpdates(prediction);

        scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.SUBMITTING);

        String gameId;
        synchronized (game.acquireLock()) {
            gameId = game.getId();
        }

        new SubmitPredictionTask(LocalAccountManager.get().getId(), gameId,
                prediction.getAwayScore(), prediction.getHomeScore(), new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    Log.e(TAG, task.getError().toString());
                    return;
                }

                Prediction.enableUpdates(prediction);

                synchronized (prediction.acquireLock()) {
                    if (!prediction.isComplete()) scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.UNPREDICTED);
                    else                          scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
                }
            }
        }).start();
    }

//    private void submitPrediction() {
//        prediction.setModified(true);
//        prediction.setOnServer(false);
//
//        lowerMiddleContainer.showLoading();
//
//        new SubmitPredictionTask(LocalAccountManager.get().getId(), game.getId(),
//                prediction.getAwayScore(), prediction.getHomeScore(), new TaskFinishedListener() {
//            @Override
//            public void onTaskFinished(BaseTask task) {
//                if (task.errorOccurred()) {
//                    // TODO: Show a snackbar or something
//                    Log.e(TAG, task.getError().toString());
//                    return;
//                }
//
//                synchronized (prediction.getLock()) {
//                    prediction.setOnServer(true);
//                    if (!prediction.isComplete()) lowerMiddleContainer.showUnpredictedText();
//                    else                          lowerMiddleContainer.showPredictedText();
//                }
//
//            }
//        }).start();
//    }

//    private void markPredictedWinner() {
//        int predictedWinner, color;
//        /* Do we need these to be synchronized? */
//        synchronized (prediction.acquireLock()) {
//            predictedWinner = prediction.winner();
//        }
//
//        switch (predictedWinner) {
//            case Prediction.W_AWAY:
//                synchronized (game.acquireLock()) {
//                    color = game.getAwayColor();
//                }
//
//                if (numberPadFragment != null)
//                    numberPadFragment.setBufferColor(color);
//
//                awayPrediction.solidBackground(color, ColorUtil.WHITE);
//                homePrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
//
//                setScoreboardColor(color);
//                break;
//
//            case Prediction.W_HOME:
//                synchronized (game.acquireLock()) {
//                    color = game.getHomeColor();
//                }
//
//                if (numberPadFragment != null)
//                    numberPadFragment.setBufferColor(color);
//
//                awayPrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
//                homePrediction.solidBackground(color, ColorUtil.WHITE);
//
//                setScoreboardColor(color);
//                break;
//
//            default:
//                if (numberPadFragment != null)
//                    numberPadFragment.setBufferColor(ColorUtil.STANDARD_TEXT);
//
//                awayPrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
//                homePrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
//
//                setScoreboardColor(ColorUtil.STANDARD_TEXT);
//        }
//    }

    public void setScoreboardColor(int color) {
        scoreboard.setColor(color);
        activity.setToolbarColor(color, this);
        tabs.setBackgroundColor(color);

        PredictionFragment predictionFragment = getPredictionFragment();
        if (predictionFragment != null) {
            Log.d(TAG, "PredictionFragment was not null");
            predictionFragment.setHeaderColor(color);
        } else {
            Log.d(TAG, "PredictionFragment was null");
        }
    }

    public int getScoreboardColor() {
        return scoreboard.getColor();
    }

    private void showNumberPadFragment(String teamName, int teamColor, boolean fromLeft) {
        numberPadFragment = NumberPadFragment.newInstance(teamName, teamColor, scoreboardColor);
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (fromLeft)
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

        fragmentTransaction.add(R.id.container, numberPadFragment);
        fragmentTransaction.commit();
    }

    private void showNumberPadFragment(Team team, int bufferColor) {
        String locale, name;
        int color;
        synchronized (game.acquireLock()) {
            switch (team) {
                case AWAY:
                    locale = game.getAwayLocale();
                    name = game.getAwayName();
                    color = game.getAwayColor();
                    break;

                case HOME:
                    locale = game.getHomeLocale();
                    name = game.getHomeName();
                    color = game.getHomeColor();
                    break;

                default:
                    throw new RuntimeException("Team value was neither HOME nor AWAY");
            }
        }

        numberPadFragment = NumberPadFragment.newInstance(locale + " " + name, color, bufferColor);
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (team == Team.AWAY) fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else                   fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

        fragmentTransaction.add(R.id.container, numberPadFragment);
        fragmentTransaction.commit();
    }

    /*
    private void showNumberPadFragment(OriginalTeam team, int bufferColor, boolean fromLeft) {
        numberPadFragment = NumberPadFragment.newInstance(team.getLocale() + " " + team.getName(), team.getPrimaryColor(), bufferColor);
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (fromLeft) fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else          fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

        fragmentTransaction.add(R.id.container, numberPadFragment);
        fragmentTransaction.commit();
    }
    */

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
    public FullGame getGame() {
        return game;
    }

    @Override
    public Prediction getPrediction() {
        return prediction;
    }

    private void addDigitToPrediction(String digit) {
        if (!inPredictSession) {
            clearPrediction();
            inPredictSession = true;
        }

        switch (selectedPredictionTeam) {
            case AWAY:
                scoreboard.appendDigitToAwayPrediction(digit);
                break;

            case HOME:
                scoreboard.appendDigitToHomePrediction(digit);
                break;
        }
    }

    @Override
    public void keyPressed(NumberPadFragment.Key k) {
        if (selectedPredictionTeam == null)
            return;

        Log.d(TAG, "Key pressed: " + k.toString());
        switch (k) {
            case KEY_ENTER:
                confirmPrediction();
                break;
            case KEY_CLEAR:
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

    public enum State {
        LOADING,
        SCOREBOARD
    }
}
