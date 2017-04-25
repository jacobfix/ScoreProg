package jacobfix.scorepredictor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.components.FlipCardView;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class ScoreboardView extends RelativeLayout {

    LinearLayout mMiddleContainer;
    LinearLayout mAwayNameContainer;
    LinearLayout mHomeNameContainer;

    TextView mQuarter;
    TextView mClock;

    TextView mAwayAbbr;
    TextView mHomeAbbr;
    TextView mAwayName;
    TextView mHomeName;

    TextView mAwayScore;
    TextView mHomeScore;
    FlipCardView mAwayFlipCard;
    FlipCardView mHomeFlipCard;

    public ScoreboardView(Context context) {
        this(context, null);
    }

    public ScoreboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews();
    }

    private void initializeViews() {
        mMiddleContainer = ViewUtil.findById(this, R.id.middle_container);
        mAwayNameContainer = ViewUtil.findById(this, R.id.away_name_container);
        mHomeNameContainer = ViewUtil.findById(this, R.id.home_name_container);

        mQuarter = ViewUtil.findById(this, R.id.quarter);
        mClock = ViewUtil.findById(this, R.id.clock);

        mAwayAbbr = ViewUtil.findById(this, R.id.away_abbr);
        mHomeAbbr = ViewUtil.findById(this, R.id.home_abbr);
        mAwayName = ViewUtil.findById(this, R.id.away_name);
        mHomeName = ViewUtil.findById(this, R.id.home_name);

        mAwayScore = ViewUtil.findById(this, R.id.away_score_actual);
        mHomeScore = ViewUtil.findById(this, R.id.home_score_actual);

        mAwayFlipCard = ViewUtil.findById(this, R.id.away_flip_card);
        mHomeFlipCard = ViewUtil.findById(this, R.id.home_flip_card);
    }

    public void updateState(NflGame game) {
        mQuarter.setText(Util.formatQuarter(getResources(), game.getQuarter()));
        mClock.setText(game.getClock());

        mAwayAbbr.setText(game.getAwayTeam().getAbbr());
        mAwayName.setText(game.getAwayTeam().getName());
        mHomeAbbr.setText(game.getHomeTeam().getAbbr());
        mHomeName.setText(game.getHomeTeam().getName());

        mAwayScore.setText(String.valueOf(game.getAwayTeam().getScore()));
        mHomeScore.setText(String.valueOf(game.getHomeTeam().getScore()));
    }

    public FlipCardView getAwayFlipCard() {
        return mAwayFlipCard;
    }

    public FlipCardView getHomeFlipCard() {
        return mHomeFlipCard;
    }
}
