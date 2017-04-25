package jacobfix.scorepredictor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.components.FlipCardView;
import jacobfix.scorepredictor.util.ViewUtil;

/**
 * View for the scoreboard in the OriginalGameActivity class.
 */
public class OriginalScoreboardView extends RelativeLayout {

    LinearLayout mAwayNameContainer;
    LinearLayout mHomeNameContainer;

    FrameLayout mUpperCenterContainer;

    // TODO: Some kind of object that establishes a size relationship between
    // TextViews, so that if one of them needs to be shrunk, they will all be shrunk
    TextView mAwayAbbr;
    TextView mHomeAbbr;
    TextView mAwayName;
    TextView mHomeName;
    TextView mAwayScore;
    TextView mHomeScore;

    FlipCardView mAwayFlipCard;
    FlipCardView mHomeFlipCard;

    FrameLayout mLowerCenterContainer;

    private static final int CENTER_CONTENT_PREGAME = 1;
    private static final int CENTER_CONTENT_IN_PROGRESS = 2;
    private static final int CENTER_CONTENT_FINAL = 3;

    public OriginalScoreboardView(Context context) {
        this(context, null);
    }

    public OriginalScoreboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OriginalScoreboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews();
    }

    private void initializeViews() {
        mAwayNameContainer = ViewUtil.findById(this, R.id.away_name_container);
        mHomeNameContainer = ViewUtil.findById(this, R.id.home_name_container);

        mUpperCenterContainer = ViewUtil.findById(this, R.id.upper_center_container);

        mAwayAbbr = ViewUtil.findById(this, R.id.away_abbr);
        mHomeAbbr = ViewUtil.findById(this, R.id.home_abbr);

        mAwayName = ViewUtil.findById(this, R.id.away_name);
        mHomeName = ViewUtil.findById(this, R.id.home_name);

        mAwayScore = ViewUtil.findById(this, R.id.away_score_actual);
        mHomeScore = ViewUtil.findById(this, R.id.home_score_actual);

        mAwayFlipCard = ViewUtil.findById(this, R.id.away_flip_card);
        mHomeFlipCard = ViewUtil.findById(this, R.id.home_flip_card);
    }

    public void update(NflGame game) {
        boolean predictionsVisible;
        int centerContent;

        if (game.isPregame()) {
            centerContent = CENTER_CONTENT_PREGAME;
            predictionsVisible = true;
        } else if (game.inProgress()) {
            centerContent = CENTER_CONTENT_IN_PROGRESS;
            if (game.isPredicted()) {
                predictionsVisible = true;
            } else {
                predictionsVisible = false;
            }
        } else {
            centerContent = CENTER_CONTENT_FINAL;
            if (game.isPredicted()) {
                predictionsVisible = true;
            } else {
                predictionsVisible = false;
            }
        }
        showPredictions(predictionsVisible);
        setCenterContent(centerContent);

        mAwayAbbr.setText(game.getAwayTeam().getAbbr());
        mHomeAbbr.setText(game.getHomeTeam().getAbbr());

        mAwayName.setText(game.getAwayTeam().getName());
        mHomeName.setText(game.getHomeTeam().getName());

        mAwayScore.setText(game.getAwayTeam().getScore());
        mHomeScore.setText(game.getHomeTeam().getScore());
    }

    public FlipCardView getAwayFlipCard() {
        return mAwayFlipCard;
    }

    public FlipCardView getHomeFlipCard() {
        return mHomeFlipCard;
    }

    private void showPredictions(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        mAwayFlipCard.setVisibility(visibility);
        mHomeFlipCard.setVisibility(visibility);
    }

    private void setCenterContent(int centerContent) {
        switch (centerContent) {
            case CENTER_CONTENT_PREGAME:
                break;
            case CENTER_CONTENT_IN_PROGRESS:
                break;
            case CENTER_CONTENT_FINAL:
                break;
        }
    }
}
