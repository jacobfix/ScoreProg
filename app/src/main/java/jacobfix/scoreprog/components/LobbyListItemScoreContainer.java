package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class LobbyListItemScoreContainer extends LinearLayout {

    private static final String TAG = LobbyListItemScoreContainer.class.getSimpleName();

    private TextView actualScore;
    private PredictionView predictedScore;

    public LobbyListItemScoreContainer(Context context) {
        this(context, null);
    }

    public LobbyListItemScoreContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LobbyListItemScoreContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        actualScore = ViewUtil.findById(this, R.id.score);
        predictedScore = ViewUtil.findById(this, R.id.prediction);

        actualScore.setTypeface(FontHelper.getArimoRegular(getContext()));
        // predictedScore.setTypeface(FontHelper.getArimoRegular(getContext()));

        /*
        actualScore.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                predictedScore.getTextView().getTextSize());
                */

        uncolor();
        Log.d(TAG, "NDS score container onFinishInflate()");
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = (int) (suggestedWidth * PredictionView.DEFAULT_SIZE_RATIO);
        ViewUtil.fitTextToWidth(actualScore, desiredWidth, "99");

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "NDS Measured list item score container");
    }

    public void color(int color) {
        setBackgroundColor(color);

        actualScore.setTextColor(ColorUtil.WHITE);
        predictedScore.solidBackground(color, ColorUtil.WHITE);
    }

    public void uncolor() {
        setBackgroundColor(ColorUtil.WHITE);

        actualScore.setTextColor(ColorUtil.STANDARD_TEXT);
        predictedScore.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
    }

    public void setActualScore(int score) {
        this.actualScore.setText(String.valueOf(score));
    }

    public void setPredictedScore(int score) {
        this.predictedScore.setScore(score);
    }

    public void showActualScore() {
        this.actualScore.setVisibility(View.VISIBLE);
    }

    public void hideActualScore() {
        this.actualScore.setVisibility(View.GONE);
    }

    public void showPredictedScore() {
        this.predictedScore.setVisibility(View.VISIBLE);
    }

    public void hidePredictedScore() {
        this.predictedScore.setVisibility(View.GONE);
    }

    public float getActualScoreTextSize() {
        return actualScore.getTextSize();
    }

    public float getPredictedScoreTextSize() {
        return predictedScore.getTextView().getTextSize();
    }

    public void setActualScoreTextSize(float textSize) {
        actualScore.setTextSize(textSize);
    }

    public void setActualScoreTextSize(int unit, float textSize) {
        actualScore.setTextSize(unit, textSize);
    }

    public void setPredictedScoreTextSize(float textSize) {
        predictedScore.getTextView().setTextSize(textSize);
    }

    public void setPredictedScoreTextSize(int unit, float textSize) {
        predictedScore.getTextView().setTextSize(unit, textSize);
    }
}
