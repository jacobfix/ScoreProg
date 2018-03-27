package jacobfix.scoreprog.components;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.Prediction;
import jacobfix.scoreprog.R;
import jacobfix.scoreprog.Team;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.Util;
import jacobfix.scoreprog.util.ViewUtil;

public class ScoreboardView extends FrameLayout {
    
    private static final String TAG = ScoreboardView.class.getSimpleName();

    private ProgressBar loadingIcon;

    private LinearLayout scoreboardContentContainer;

    private ConstraintLayout topHalf;
    private ConstraintLayout bottomHalf;

    private ScoreboardNameContainer awayNameContainer;
    private ScoreboardNameContainer homeNameContainer;

    private ScoreboardScoreContainer awayScoreContainer;
    private ScoreboardScoreContainer homeScoreContainer;

    private ScoreboardGameStateContainer gameStateContainer;

    // predictionMiddleContainer

    private LinearLayout awayPredictionContainer;
    private LinearLayout homePredictionContainer;

    private PredictionView awayPredictionView;
    private PredictionView homePredictionView;

    private ScoreboardPredictionStatusContainer predictionStatusContainer;
    
    private int scoreboardColor;

    public static final int DEFAULT_COLOR = ColorUtil.DEFAULT_SCOREBOARD_COLOR;

    public ScoreboardView(Context context) {
        this(context, null);
    }

    public ScoreboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        ConstraintLayout topHalf = ViewUtil.findById(this, R.id.top_half);
        ConstraintLayout bottomHalf = ViewUtil.findById(this, R.id.bottom_half);

        Log.d(TAG, "Top half:");
        Log.d(TAG, "    Height: " + topHalf.getHeight());
        Log.d(TAG, "Bottom half:");
        Log.d(TAG, "    Height: " + bottomHalf.getHeight());

        int outerPadding = (int) Util.dpToPx(getContext(), 15);
        int innerPadding = (int) Util.dpToPx(getContext(), 5);

        awayNameContainer.setPadding(outerPadding, 0, innerPadding, 0);
        homeNameContainer.setPadding(innerPadding, 0, outerPadding, 0);

        awayNameContainer.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        homeNameContainer.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        scoreboardContentContainer = ViewUtil.findById(this, R.id.main_content_container);
        loadingIcon = ViewUtil.findById(this, R.id.loading_icon);

        topHalf = ViewUtil.findById(this, R.id.top_half);
        bottomHalf = ViewUtil.findById(this, R.id.bottom_half);

        awayNameContainer = ViewUtil.findById(this, R.id.away_name_container);
        homeNameContainer = ViewUtil.findById(this, R.id.home_name_container);
        awayNameContainer.setTag(Team.AWAY);
        homeNameContainer.setTag(Team.HOME);

        awayScoreContainer = ViewUtil.findById(this, R.id.away_score_container);
        homeScoreContainer = ViewUtil.findById(this, R.id.home_score_container);
        awayScoreContainer.setTag(Team.AWAY);
        homeScoreContainer.setTag(Team.HOME);

        gameStateContainer = ViewUtil.findById(this, R.id.game_state_container);

        awayPredictionContainer = ViewUtil.findById(this, R.id.away_prediction_container);
        homePredictionContainer = ViewUtil.findById(this, R.id.home_prediction_container);
        awayPredictionContainer.setTag(Team.AWAY);
        homePredictionContainer.setTag(Team.HOME);

        awayPredictionView = ViewUtil.findById(this, R.id.away_prediction);
        homePredictionView = ViewUtil.findById(this, R.id.home_prediction);
        awayPredictionView.setTag(Team.AWAY);
        homePredictionView.setTag(Team.HOME);

        predictionStatusContainer = ViewUtil.findById(this, R.id.prediction_status_container);
    }

    public void updateGameState(String awayAbbr, String homeAbbr, String awayName, String homeName,
                                int awayScore, int homeScore, boolean locked, Game.State gameState,
                                long startTime, String clock, int quarter) {

        if (locked) gameStateContainer.showLock();
        else        gameStateContainer.hideLock();

        switch (gameState) {
            case PREGAME:
                awayScoreContainer.hideScore();
                homeScoreContainer.hideScore();
                gameStateContainer.pregameDisplay(startTime);
                break;

            case FINAL:
                awayScoreContainer.showScore();
                homeScoreContainer.showScore();
                gameStateContainer.finalDisplay();
                break;

            case IN_PROGRESS:
                awayScoreContainer.showScore();
                homeScoreContainer.showScore();
                gameStateContainer.inProgressDisplay(clock, quarter);
                break;
        }

        awayNameContainer.setName(awayAbbr, awayName);
        homeNameContainer.setName(homeAbbr, homeName);

        awayScoreContainer.setScore(awayScore);
        homeScoreContainer.setScore(homeScore);
    }

    public void updatePrediction(int awayPredictedScore, int homePredictedScore, boolean complete,
                                 int awayScore, int homeScore, boolean locked,
                                 int awayColor, int homeColor) {
        if (locked) {
            if (!complete) {
                bottomHalf.setVisibility(View.GONE);
            } else {
                setPredictionState(ScoreboardPredictionStatusContainer.State.SPREAD);
                bottomHalf.setVisibility(View.VISIBLE);
            }
        } else {
            bottomHalf.setVisibility(View.VISIBLE);
            if (!complete) {
                setPredictionState(ScoreboardPredictionStatusContainer.State.UNPREDICTED);
            } else {
                setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
            }
        }

        awayPredictionView.setScore(awayPredictedScore);
        homePredictionView.setScore(homePredictedScore);

        int spread = Prediction.computeSpread(awayPredictedScore, homePredictedScore,
                awayScore, homeScore);
        predictionStatusContainer.setSpread(spread);

        int diff = awayPredictedScore - homePredictedScore;
        if (diff > 0) {
            awayPredictionView.solidBackground(awayColor, ColorUtil.WHITE);
            homePredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
        } else if (diff < 0) {
            awayPredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
            homePredictionView.solidBackground(homeColor, ColorUtil.WHITE);
        } else {
            awayPredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
            homePredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
        }
    }

    public void updatePrediction(Prediction prediction, boolean gameLocked,
                                 int awayScore, int homeScore) {
        int predictedWinner;
        synchronized (prediction.acquireLock()) {
            if (gameLocked) {
                if (!prediction.isComplete()) {
                    bottomHalf.setVisibility(View.GONE);
                } else {
                    setPredictionState(ScoreboardPredictionStatusContainer.State.SPREAD);
                    bottomHalf.setVisibility(View.VISIBLE);
                }
            } else {
                bottomHalf.setVisibility(View.VISIBLE);
                if (!prediction.isComplete()) {
                    setPredictionState(ScoreboardPredictionStatusContainer.State.UNPREDICTED);
                } else {
                    setPredictionState(ScoreboardPredictionStatusContainer.State.PREDICTED);
                }
            }

            awayPredictionView.setScore(prediction.getAwayScore());
            homePredictionView.setScore(prediction.getHomeScore());

            int spread = Prediction.computeSpread(prediction.getAwayScore(), prediction.getHomeScore(), awayScore, homeScore);
            predictionStatusContainer.setSpread(spread);

            switch (prediction.winner()) {
                case Prediction.W_AWAY:
                    awayPredictionView.solidBackground(ColorUtil.TRANSPARENT, ColorUtil.WHITE);
                    homePredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
                    break;

                case Prediction.W_HOME:
                    awayPredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
                    homePredictionView.solidBackground(ColorUtil.TRANSPARENT, ColorUtil.WHITE);
                    break;

                default:
                    awayPredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
                    homePredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
            }
        }
    }

    public void highlightAwayPrediction(int color) {
        awayPredictionView.solidBackground(color, ColorUtil.WHITE);
        homePredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
    }

    public void highlightHomePrediction(int color) {
        awayPredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
        homePredictionView.solidBackground(color, ColorUtil.WHITE);
    }

    public void highlightNeitherPrediction() {
        awayPredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
        homePredictionView.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
    }

    public void appendDigitToAwayPrediction(String digit) {
        awayPredictionView.append(digit);
    }

    public void appendDigitToHomePrediction(String digit) {
        homePredictionView.append(digit);
    }

    public void clearAwayPrediction() {
        awayPredictionView.clear();
    }

    public void clearHomePrediction() {
        homePredictionView.clear();
    }

    public int getAwayPredictedScore() {
        return awayPredictionView.getScore();
    }

    public int getHomePredictedScore() {
        return homePredictionView.getScore();
    }

    public void setPredictionState(ScoreboardPredictionStatusContainer.State state) {
        predictionStatusContainer.setState(state);
    }

    public void show() {
        // TODO: Have an animation when the scoreboard changes state
        scoreboardContentContainer.setVisibility(View.VISIBLE);
//        ObjectAnimator colorFade = ObjectAnimator.ofObject(scoreboardContentContainer, "backgroundColor", new ArgbEvaluator(), ScoreboardView.DEFAULT_COLOR, scoreboardColor);
//        colorFade.setDuration(500);
//        colorFade.start();
        loadingIcon.setVisibility(View.GONE);
    }

    public void loading() {
        scoreboardContentContainer.setVisibility(View.INVISIBLE);
        loadingIcon.setVisibility(View.VISIBLE);
    }
    
    public void setColor(int color) {
        setBackgroundColor(color);
        scoreboardColor = color;
    }

    public int getColor() {
        return scoreboardColor;
    }

    public void setAwayName(String abbr, String name) {
        awayNameContainer.setName(abbr, name);
    }

    public void setHomeName(String abbr, String name) {
        homeNameContainer.setName(abbr, name);
    }

    public void setAwayScore(int score) {
        awayScoreContainer.setScore(score);
    }

    public void setHomeScore(int score) {
        homeScoreContainer.setScore(score);
    }

    public void showLock() {
        gameStateContainer.showLock();
    }

    public void hideLock() {
        gameStateContainer.hideLock();
    }

    public void showScores() {
        awayScoreContainer.showScore();
        homeScoreContainer.showScore();
    }

    public void hideScores() {
        awayScoreContainer.hideScore();
        homeScoreContainer.hideScore();
    }

    public void showPredictions() {
        bottomHalf.setVisibility(View.VISIBLE);
    }

    public void hidePredictions() {
        bottomHalf.setVisibility(View.GONE);
    }

    public void setPredictionSpread(int spread) {
        Log.d(TAG, "PredictionSpread: SETTING");
        predictionStatusContainer.setSpread(spread);
    }

    public void pregameDisplay(long startTime) {
        gameStateContainer.pregameDisplay(startTime);
    }

    public void inProgressDisplay(String clock, int quarter) {
        gameStateContainer.inProgressDisplay(clock, quarter);
    }

    public void finalDisplay() {
        gameStateContainer.finalDisplay();
    }

    public void setAwayPrediction(int predictedScore) {
        awayPredictionView.setScore(predictedScore);
    }

    public void setHomePrediction(int predictedScore) {
        homePredictionView.setScore(predictedScore);
    }

    public void setTeamSelectedListener(final TeamSelectedListener listener) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTeamSelected((Team) view.getTag());
            }
        };

        awayNameContainer.setOnClickListener(onClickListener);
        homeNameContainer.setOnClickListener(onClickListener);

        awayScoreContainer.setOnClickListener(onClickListener);
        homeScoreContainer.setOnClickListener(onClickListener);

        awayPredictionContainer.setOnClickListener(onClickListener);
        homePredictionContainer.setOnClickListener(onClickListener);
    }

    public interface TeamSelectedListener {
        void onTeamSelected(Team teamSelected);
    }
}
