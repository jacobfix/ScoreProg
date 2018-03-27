package jacobfix.scoreprog;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import jacobfix.scoreprog.components.ScoreboardPredictionStatusContainer;
import jacobfix.scoreprog.components.ScoreboardView;
import jacobfix.scoreprog.sync.GameAndDriveFeed;
import jacobfix.scoreprog.sync.GameProvider;
import jacobfix.scoreprog.sync.PredictionProvider;
import jacobfix.scoreprog.task.BaseTask;
import jacobfix.scoreprog.task.SubmitPredictionTask;
import jacobfix.scoreprog.task.TaskFinishedListener;
import jacobfix.scoreprog.util.ViewUtil;

import static jacobfix.scoreprog.Game.State.PREGAME;
import static jacobfix.scoreprog.Prediction.W_AWAY;
import static jacobfix.scoreprog.Prediction.W_HOME;
import static jacobfix.scoreprog.Prediction.W_TIE;

public class GameFragment extends Fragment implements PredictionFragment.PredictionFragmentListener,
        NumberPadFragment.NumberPadFragmentListener, PlayFeedFragment.PlayFeedFragmentListener {

    private static final String TAG = GameFragment.class.getSimpleName();

    // TODO: Maybe have two separate entities, a Game and FullGameDetails
    // private FullGame game;

    private Game game;
    private DriveFeed driveFeed;

    private Prediction prediction;
    private Participants participants;

    // private Map<String, Prediction> participants;

    private Team selectedPredictionTeam;
    private boolean predictionsModified;
    private boolean inPredictSession;
    private int confirmedAwayScore;
    private int confirmedHomeScore;

    private ViewGroup root;
    private ScoreboardView scoreboard;

    private ViewPager pager;
    private GameFragmentPagerAdapter pagerAdapter;
    private TabLayout tabs;

    private int scoreboardColor;

    private GameActivity activity;
    private NumberPadFragment numberPadFragment;
    private PredictionFragment predictionFragment;

    private PredictionFragment.State predictionFragmentState = PredictionFragment.State.LOADING;

    private State state;

    private boolean predictionRetrieved;
    private boolean uiInitialized;

    private AsyncCallback<Collection<GameAndDriveFeed>> gameSyncCallback = new AsyncCallback<Collection<GameAndDriveFeed>>() {
        @Override
        public void onSuccess(Collection<GameAndDriveFeed> result) {
            GameDetails gameDetails = GameDetails.capture(game);
            PredictionDetails predictionDetails = PredictionDetails.capture(prediction);

            updateScoreboardGameState(gameDetails);
            updateScoreboardPredictionState(gameDetails, predictionDetails);

            // updateScoreboardDisplay(false);
            // updateScoreboardPredictionState();

            updatePredictionFragmentGameState(); // TODO: Pass GameDetails/PredictionDetails?
            if (gameDetails.locked)
                updatePredictionFragmentParticipants();
        }

        @Override
        public void onFailure(Exception e) {
            Snackbar.make(root, R.string.sync_error, Snackbar.LENGTH_LONG);
            Log.e(TAG, e.toString());
        }
    };

    private AsyncCallback<Collection<Participants>> participantsSyncCallback = new AsyncCallback<Collection<Participants>>() {
        @Override
        public void onSuccess(Collection<Participants> result) {
            updatePredictionFragmentParticipants();

            /* If game is locked when this sync returns, then unregister this listener, because not additional users
               will be joining the game. */
            boolean gameLocked;
            synchronized (game.acquireLock()) {
                gameLocked = game.isLocked();
            }

            if (gameLocked) {
                Log.d(TAG, "Unregistering participants sync listener because game is locked");
                PredictionProvider.unregisterParticipantsSyncListener(this);
            }
        }

        @Override
        public void onFailure(Exception e) {
            Snackbar.make(root, R.string.sync_error, Snackbar.LENGTH_LONG);
        }
    };

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
        activity = (GameActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        pagerAdapter = new GameFragmentPagerAdapter(getChildFragmentManager());

        final String gameId = getArguments().getString("gameId");
        game = GameProvider.getGame(gameId);

        GameProvider.getDriveFeed(gameId, new AsyncCallback<DriveFeed>() {
            @Override
            public void onSuccess(DriveFeed result) {
                driveFeed = result;
                updatePlayFeedFragmentDriveFeed();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
                Snackbar.make(root, R.string.sync_error, Snackbar.LENGTH_LONG);
            }
        });

        PredictionProvider.getMyPrediction(gameId, new AsyncCallback<Prediction>() {
            @Override
            public void onSuccess(Prediction result) {
                prediction = (result != null) ? result : new Prediction(LocalAccountManager.get().userId, gameId);
                confirmedAwayScore = prediction.getAwayScore();
                confirmedHomeScore = prediction.getHomeScore();
                predictionRetrieved = true;
                if (uiInitialized)
                    onInitFinished();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
                Snackbar.make(root, R.string.sync_error, Snackbar.LENGTH_LONG);
            }
        });

        PredictionProvider.getParticipants(gameId, new AsyncCallback<Participants>() {
            @Override
            public void onSuccess(Participants result) {
                participants = result;
                updatePredictionFragmentParticipants();

                /* Only register the participants sync callback if this game is not locked (i.e., if it is still possible
                   for additional users to participate). */
                boolean gameLocked;
                synchronized (game.acquireLock()) {
                    gameLocked = game.isLocked();
                }
                if (!gameLocked) {
                    Log.d(TAG, "Registering participants sync listener because game is not locked");
                    PredictionProvider.addGameToParticipantsSync(gameId);
                    PredictionProvider.registerParticipantsSyncListener(participantsSyncCallback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.toString());
                Snackbar.make(root, R.string.sync_error, Snackbar.LENGTH_LONG);
            }
        });

        GameProvider.registerGameSyncListener(gameSyncCallback);
    }

    private void onInitFinished() {
        GameDetails gameDetails = GameDetails.capture(game);
        PredictionDetails predictionDetails = PredictionDetails.capture(prediction);

        updateScoreboardGameState(gameDetails);
        updateScoreboardPredictedScores(gameDetails, predictionDetails);
        updateScoreboardPredictionState(gameDetails, predictionDetails);

        scoreboard.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        GameProvider.unregisterGameSyncListener(gameSyncCallback);

        PredictionProvider.removeGameFromParticipantsSync(game.getId());
        PredictionProvider.unregisterParticipantsSyncListener(participantsSyncCallback);
    }

    public void onGameLocked(String lockedGameId) {
        String thisGameId;
        synchronized (game.acquireLock()) {
            thisGameId = game.getId();
        }

        if (thisGameId.equals(lockedGameId)) {
            if (numberPadFragment != null)
                hideNumberPadFragment(selectedPredictionTeam == Team.AWAY);

            GameDetails gameDetails = GameDetails.capture(game);
            PredictionDetails predictionDetails = PredictionDetails.capture(prediction);

            updateScoreboardGameState(gameDetails);
            updateScoreboardPredictedScores(gameDetails, predictionDetails);
            updateScoreboardPredictionState(gameDetails, predictionDetails);

            updatePredictionFragmentGameState();
            updatePredictionFragmentParticipants();
            updatePlayFeedFragmentDriveFeed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_new, container, false);
        root = ViewUtil.findById(view, R.id.root);
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
        // pagerAdapter = new GameFragmentPagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);

        tabs = ViewUtil.findById(view, R.id.tabs);
        tabs.setupWithViewPager(pager);

        setScoreboardColor(ScoreboardView.DEFAULT_COLOR);
        scoreboard.loading();

        uiInitialized = true;
        if (predictionRetrieved)
            onInitFinished();

        return view;
    }

    private void confirmPrediction() {
        /* When a prediction is confirmed, we update the Prediction object with the values entered
           in each PredictionView on the scoreboard. confirmPrediction() is called each time a
           single PredictionView has been updated. */
        int awayPredictionEntered = scoreboard.getAwayPredictedScore();
        int homePredictionEntered = scoreboard.getHomePredictedScore();

        int awayPredictionConfirmed, homePredictionConfirmed;
        synchronized (prediction.acquireLock()) {
            awayPredictionConfirmed = prediction.getAwayScore();
            homePredictionConfirmed = prediction.getHomeScore();
        }

        if (awayPredictionConfirmed == awayPredictionEntered && homePredictionConfirmed == homePredictionEntered) {
            /* Prediction has not been modified, so don't submit it. */
            hideNumberPadFragment(selectedPredictionTeam == Team.AWAY);
            selectedPredictionTeam = null;
            inPredictSession = false;
            return;
        }

        submitPrediction(scoreboard.getAwayPredictedScore(), scoreboard.getHomePredictedScore());

        int predictedWinner = Prediction.winner(awayPredictionEntered, homePredictionEntered);
        highlightPredictedWinner(predictedWinner);

//        int predictedWinner;
//        int difference = awayPredictionEntered - homePredictionEntered;
//        if (difference > 0)
//            predictedWinner = W_AWAY;
//        else if (difference < 0)
//            predictedWinner = W_HOME;
//        else
//            predictedWinner = W_TIE;
//        highlightPredictedWinner(predictedWinner);

//        synchronized (prediction.acquireLock()) {
//            switch (selectedPredictionTeam) {
//                case AWAY:
//                    prediction.setAwayScore(scoreboard.getAwayPredictedScore());
//                    break;
//
//                case HOME:
//                    prediction.setHomeScore(scoreboard.getHomePredictedScore());
//                    break;
//            }
//            predictedWinner = prediction.winner();
//        }
//        submitPrediction();
//        highlightPredictedWinner(predictedWinner);

        hideNumberPadFragment(selectedPredictionTeam == Team.AWAY);
        selectedPredictionTeam = null;
        inPredictSession = false;
    }

    private void clearPrediction() {
//        synchronized (prediction) {
//            switch (selectedPredictionTeam) {
//                case AWAY:
//                    prediction.setAwayScore(Prediction.NULL);
//                    scoreboard.clearAwayPrediction();
//                    break;
//
//                case HOME:
//                    prediction.setHomeScore(Prediction.NULL);
//                    scoreboard.clearHomePrediction();
//                    break;
//            }
//        }

        switch (selectedPredictionTeam) {
            case AWAY:
                scoreboard.clearAwayPrediction();
                break;

            case HOME:
                scoreboard.clearHomePrediction();
                break;
        }
    }

    private void highlightPredictedWinner(int predictedWinner) {
        int color;
        switch (predictedWinner) {
            case Prediction.W_NONE:
                color = ScoreboardView.DEFAULT_COLOR;
                scoreboard.highlightNeitherPrediction();
                break;

            case W_AWAY:
                synchronized (game.acquireLock()) {
                    color = game.getAwayColor();
                }
                scoreboard.highlightAwayPrediction(color);
                break;

            case W_HOME:
                synchronized (game.acquireLock()) {
                    color = game.getHomeColor();
                }
                scoreboard.highlightHomePrediction(color);
                break;

            case W_TIE:
                color = ScoreboardView.DEFAULT_COLOR;
                scoreboard.highlightNeitherPrediction();
                break;

            default:
                throw new RuntimeException();
        }
        setScoreboardColor(color);
    }

    private void updateScoreboardGameState(GameDetails gameDetails) {
        if (gameDetails.locked) scoreboard.showLock();
        else                  scoreboard.hideLock();

        scoreboard.setAwayName(gameDetails.awayAbbr, gameDetails.awayName);
        scoreboard.setHomeName(gameDetails.homeAbbr, gameDetails.homeName);

        scoreboard.setAwayScore(gameDetails.awayScore);
        scoreboard.setHomeScore(gameDetails.homeScore);

        switch (gameDetails.state) {
            case PREGAME:
                scoreboard.hideScores();
                scoreboard.pregameDisplay(gameDetails.startTime);
                break;
            case FINAL:
                scoreboard.showScores();
                scoreboard.finalDisplay();
                break;
            case IN_PROGRESS:
                scoreboard.showScores();
                scoreboard.inProgressDisplay(gameDetails.clock, gameDetails.quarter);
        }
    }

    private void updateScoreboardPredictedScores(GameDetails gameDetails, PredictionDetails predictionDetails) {
        if (gameDetails.locked) {
            if (!predictionDetails.complete) {
                scoreboard.hidePredictions();
            } else {
                scoreboard.showPredictions();
                if (gameDetails.state == PREGAME) {
                    scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
                } else {
                    scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.SPREAD);
                }
            }
        } else {
            scoreboard.showPredictions();
            if (!predictionDetails.complete) {
                scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.UNPREDICTED);
            } else {
                scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
            }
        }

        scoreboard.setAwayPrediction(predictionDetails.awayScore);
        scoreboard.setHomePrediction(predictionDetails.homeScore);

        highlightPredictedWinner(predictionDetails.winner);
    }

    private void updateScoreboardPredictionState(GameDetails gameDetails, PredictionDetails predictionDetails) {
        int spread = Prediction.computeSpread(predictionDetails.awayScore,
                predictionDetails.homeScore, gameDetails.awayScore, gameDetails.homeScore);
        scoreboard.setPredictionSpread(spread);
    }

//    private void updateScoreboardDisplay(boolean predictedScores, boolean predictionSpread) {
//        boolean locked;
//        int awayScore, homeScore;
//        int awayColor, homeColor;
//        Game.State gameState;
//        synchronized (game.acquireLock()) {
//            locked = game.isLocked();
//
//            if (locked) scoreboard.showLock();
//            else        scoreboard.hideLock();
//
//            scoreboard.setAwayName(game.getAwayAbbr(), game.getAwayName());
//            scoreboard.setHomeName(game.getHomeAbbr(), game.getHomeName());
//
//            awayScore = game.getAwayScore();
//            homeScore = game.getHomeScore();
//            scoreboard.setAwayScore(awayScore);
//            scoreboard.setHomeScore(homeScore);
//
//            awayColor = game.getAwayColor();
//            homeColor = game.getHomeColor();
//
//            if (game.isPregame()) {
//                scoreboard.hideScores();
//                scoreboard.pregameDisplay(game.getStartTime());
//                gameState = PREGAME;
//            } else if (game.isFinal()) {
//                scoreboard.showScores();
//                scoreboard.finalDisplay();
//                gameState = Game.State.FINAL;
//            } else {
//                scoreboard.showScores();
//                scoreboard.inProgressDisplay(game.clock, game.getQuarter());
//                gameState = Game.State.IN_PROGRESS;
//            }
//        }
//
//        if (predictedScores) {
//            int predictedWinner;
//            synchronized (prediction.acquireLock()) {
//                if (locked) {
//                    if (!prediction.isComplete()) {
//                        /* Game is locked and a prediction was not made. Don't show the incomplete
//                           predictions; hide the lower half of the scoreboard. */
//                        scoreboard.hidePredictions();
//                    } else {
//                        scoreboard.showPredictions();
//                        if (gameState == PREGAME) {
//                            Log.d(TAG, "Game state is PREGAME");
//                            scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
//                        } else {
//                            Log.d(TAG, "Game state is not PREGAME");
//                            scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.SPREAD);
//                        }
//                    }
//                } else {
//                    /* Game is not locked, so a prediction can still be made. Show the blank predictions
//                       while it is still possible for them to be set. */
//                    scoreboard.showPredictions();
//                    if (!prediction.isComplete()) {
//                        scoreboard.setPredictionState(ScoreboardPredictionStatusContainer
//                                .State.UNPREDICTED);
//                    } else {
//                        scoreboard.setPredictionState(ScoreboardPredictionStatusContainer
//                                .State.PREDICTED);
//                    }
//                }
//
//                scoreboard.setAwayPrediction(prediction.getAwayScore());
//                scoreboard.setHomePrediction(prediction.getHomeScore());
//
//                int spread = Prediction.computeSpread(prediction.getAwayScore(),
//                        prediction.getHomeScore(), awayScore, homeScore);
//                scoreboard.setPredictionSpread(spread);
//
//                predictedWinner = prediction.winner();
//            }
//        }
//    }

//    private void updateScoreboardPredictionState() {
//        synchronized (game.acquireLock()) {
//            if (game.isPregame()) {
//                scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
//            } else {
//                scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.SPREAD);
//            }
//        }
//    }

//    private void submitPrediction() {
//        Prediction.disableUpdates(prediction);
//
//        scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.SUBMITTING);
//
//        String gameId;
//        synchronized (game.acquireLock()) {
//            gameId = game.getId();
//        }
//
//        new SubmitPredictionTask(gameId, prediction.getAwayScore(), prediction.getHomeScore(), new TaskFinishedListener() {
//            @Override
//            public void onTaskFinished(BaseTask task) {
//                if (task.errorOccurred()) {
//                    Log.e(TAG, task.getError().toString());
//                    Snackbar.make(root, "An error occurred while attempting to submit this prediction.", Snackbar.LENGTH_LONG);
//
//                    Prediction.enableUpdates(prediction);
//                    synchronized (prediction.acquireLock()) {
//                        scoreboard.setAwayScore(prediction.getAwayScore());
//                        scoreboard.setHomeScore(prediction.getHomeScore());
//                    }
//                    return;
//                }
//
//                Prediction.enableUpdates(prediction);
//
//                synchronized (prediction.acquireLock()) {
//                    if (!prediction.isComplete()) scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.UNPREDICTED);
//                    else                          scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
//                }
//            }
//        }).start();
//    }

    private void submitPrediction(final int awayScore, final int homeScore) {
        Prediction.disableUpdates(prediction);

        scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.SUBMITTING);

        String gameId;
        synchronized (game.acquireLock()) {
            gameId = game.getId();
        }

        new SubmitPredictionTask(gameId, awayScore, homeScore, new TaskFinishedListener() {
            @Override
            public void onTaskFinished(BaseTask task) {
                if (task.errorOccurred()) {
                    Log.e(TAG, task.getError().toString());
                    Snackbar.make(root, "An error occurred while attempting to submit this prediction.", Snackbar.LENGTH_LONG);

                    Prediction.enableUpdates(prediction);

                    /* Prediction failed to submit, so revert the predictions displayed on the scoreboard to the last predicted scores that
                       were successfully submitted. */
                    GameDetails gameDetails = GameDetails.capture(game);
                    PredictionDetails predictionDetails = PredictionDetails.capture(prediction);
                    updateScoreboardPredictedScores(gameDetails, predictionDetails);
                    updateScoreboardPredictionState(gameDetails, predictionDetails);
//                    int predictedWinner;
//                    synchronized (prediction.acquireLock()) {
//                        scoreboard.setAwayScore(prediction.getAwayScore());
//                        scoreboard.setHomeScore(prediction.getHomeScore());
//                        predictedWinner = prediction.winner();
//                    }
//                    highlightPredictedWinner(predictedWinner);
                    return;
                }

                Prediction.enableUpdates(prediction);

                /* Predicted scores were successfully written to the server, so update our local Prediction object. */
                int predictedWinner;
                synchronized (prediction.acquireLock()) {
                    prediction.setAwayScore(awayScore);
                    prediction.setHomeScore(homeScore);
                    if (!prediction.isComplete()) scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.UNPREDICTED);
                    else                          scoreboard.setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
                    predictedWinner = prediction.winner();
                }
                highlightPredictedWinner(predictedWinner);
            }
        }).start();
    }

    private void cancelPrediction() {
        GameDetails gameDetails = GameDetails.capture(game);
        PredictionDetails predictionDetails = PredictionDetails.capture(prediction);
        updateScoreboardPredictedScores(gameDetails, predictionDetails);
        updateScoreboardPredictionState(gameDetails, predictionDetails);

        hideNumberPadFragment(selectedPredictionTeam == Team.AWAY);
        selectedPredictionTeam = null;
        inPredictSession = false;
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
        if (numberPadFragment == null)
            return;

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        if (fromLeft) fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
        else          fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right);

        fragmentTransaction.remove(numberPadFragment);
        fragmentTransaction.commit();

        numberPadFragment = null;
    }

    private void updatePlayFeedFragmentDriveFeed() {
        PlayFeedFragment fragment = getPlayFeedFragment();
        if (fragment != null)
            fragment.updateDriveFeed(driveFeed);
    }

    // TODO: Maybe we should pass in GameDetails object
    private void updatePredictionFragmentGameState() {
        PredictionFragment fragment = getPredictionFragment();
        if (fragment != null)
            fragment.onGameStateChanged(game);
    }

    private void updatePredictionFragmentParticipants() {
        PredictionFragment fragment = getPredictionFragment();
        if (fragment != null)
            fragment.updateParticipants(participants);
        showPredictionFragmentList();
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
    public Participants getParticipants() {
        return participants;
    }

    private PredictionFragment getPredictionFragment() {
        return (PredictionFragment) pagerAdapter.getRegisteredFragment(GameFragmentPagerAdapter.FRIENDS_PAGE);
    }

    private PlayFeedFragment getPlayFeedFragment() {
        return (PlayFeedFragment) pagerAdapter.getRegisteredFragment(GameFragmentPagerAdapter.LIVE_PAGE);
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public Prediction getPrediction() {
        return prediction;
    }

    public DriveFeed getDriveFeed() {
        return driveFeed;
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
            case KEY_CANCEL:
                cancelPrediction();
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
