package jacobfix.scorepredictor;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.util.ColorUtil;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class ScoreboardView extends FrameLayout {

    private RelativeLayout upperScoreboard;
    private RelativeLayout lowerScoreboard;

    private UpperMiddleContainer upperMiddleContainer;
    private LowerMiddleContainer lowerMiddleContainer;

    private RelativeLayout awayScoreContainer;
    private RelativeLayout homeScoreContainer;

    private FrameLayout awayPredictionContainer;
    private FrameLayout homePredictionContainer;

    private LinearLayout awayNameContainer;
    private LinearLayout homeNameContainer;

    private TextView quarter;
    private TextView clock;

    private TextView awayAbbr;
    private TextView homeAbbr;
    private TextView awayName;
    private TextView homeName;
    private TextView awayScore;
    private TextView homeScore;

    private PredictionView awayPrediction;
    private PredictionView homePrediction;

    public ScoreboardView(Context context) {
        super(context);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        upperScoreboard = ViewUtil.findById(this, R.id.upper_scoreboard_container);
        lowerScoreboard = ViewUtil.findById(this, R.id.lower_scoreboard_container);

        upperMiddleContainer = ViewUtil.findById(this, R.id.upper_middle_container);
        lowerMiddleContainer = ViewUtil.findById(this, R.id.lower_middle_container);

        awayNameContainer = ViewUtil.findById(this, R.id.away_name_container);
        homeNameContainer = ViewUtil.findById(this, R.id.home_name_container);

        awayAbbr = ViewUtil.findById(this, R.id.away_abbr);
        homeAbbr = ViewUtil.findById(this, R.id.home_abbr);
        awayName = ViewUtil.findById(this, R.id.away_name);
        homeName = ViewUtil.findById(this, R.id.home_name);
        
        awayScore = ViewUtil.findById(this, R.id.away_score_actual);
        homeScore = ViewUtil.findById(this, R.id.home_score_actual);

        awayPrediction = ViewUtil.findById(this, R.id.away_flip_card);
        homePrediction = ViewUtil.findById(this, R.id.home_flip_card);

        Context context = getContext();

        awayAbbr.setTypeface(FontHelper.getYantramanavBold(context));
        homeAbbr.setTypeface(FontHelper.getYantramanavBold(context));
        awayName.setTypeface(FontHelper.getYantramanavBold(context));
        homeName.setTypeface(FontHelper.getYantramanavBold(context));

        awayScore.setTypeface(FontHelper.getYantramanavRegular(context));
        homeScore.setTypeface(FontHelper.getYantramanavRegular(context));

        awayPrediction.setTypeface(FontHelper.getYantramanavRegular(context));
        homePrediction.setTypeface(FontHelper.getYantramanavRegular(context));

        awayPrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);
        homePrediction.strokedBackground(ColorUtil.WHITE, ColorUtil.WHITE);

        // TODO: Deboss
    }
}
