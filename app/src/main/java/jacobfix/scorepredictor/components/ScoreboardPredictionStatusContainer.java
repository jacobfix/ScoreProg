package jacobfix.scorepredictor.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import jacobfix.scorepredictor.R;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class ScoreboardPredictionStatusContainer extends FrameLayout {

    private static final String TAG = ScoreboardPredictionStatusContainer.class.getSimpleName();

    private TextView predictedLabel;
    private ProgressBar submittingIcon;
    private ProgressBar predictionSpread;

    public enum State {
        SUBMITTING,
        UNPREDICTED,
        PREDICTED,
        SPREAD
    }

    public ScoreboardPredictionStatusContainer(Context context) {
        this(context, null);
    }

    public ScoreboardPredictionStatusContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreboardPredictionStatusContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        predictedLabel = ViewUtil.findById(this, R.id.predicted_label);
        submittingIcon = ViewUtil.findById(this, R.id.submitting_icon);
        predictionSpread = ViewUtil.findById(this, R.id.prediction_spread);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "Left padding: " + getPaddingLeft() + ", right padding: " + getPaddingRight());
        int desiredWidth = suggestedWidth - getPaddingLeft() - getPaddingRight();

        predictedLabel.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        ViewUtil.fitTextToWidth(predictedLabel, desiredWidth, "UNPREDICTED");

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setState(State state) {
        switch (state) {
            case UNPREDICTED:
                predictedLabel.setVisibility(View.VISIBLE);
                predictedLabel.setText("Unpredicted");

                submittingIcon.setVisibility(View.GONE);
                predictionSpread.setVisibility(View.GONE);
                break;

            case PREDICTED:
                predictedLabel.setVisibility(View.VISIBLE);
                predictedLabel.setText("Predicted");

                submittingIcon.setVisibility(View.GONE);
                predictionSpread.setVisibility(View.GONE);
                break;

            case SUBMITTING:
                submittingIcon.setVisibility(View.VISIBLE);

                predictedLabel.setVisibility(View.GONE);
                predictionSpread.setVisibility(View.GONE);
                break;

            case SPREAD:
                predictionSpread.setVisibility(View.VISIBLE);

                predictedLabel.setVisibility(View.GONE);
                submittingIcon.setVisibility(View.GONE);
                break;
        }
    }

    public void setSpread(int spread) {

    }
}
