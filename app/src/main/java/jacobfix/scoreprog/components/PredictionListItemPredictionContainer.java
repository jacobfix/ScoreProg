package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.ViewUtil;

public class PredictionListItemPredictionContainer extends FrameLayout {

    private PredictionView prediction;

    private int originalBackgroundColor;

    // private static final float PREDICTION_VIEW_SIZE_RATIO = 0.6f;

    public PredictionListItemPredictionContainer(Context context) {
        this(context, null);
    }

    public PredictionListItemPredictionContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PredictionListItemPredictionContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void onFinishInflate() {
        super.onFinishInflate();

        prediction = ViewUtil.findById(this, R.id.prediction);
        // prediction.setSizeRatio(PREDICTION_VIEW_SIZE_RATIO);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void showPrediction() {
        prediction.setVisibility(View.VISIBLE);
    }

    public void hidePrediction() {
        prediction.setVisibility(View.GONE);
    }

    public void setPredictedScore(int predictedScore) {
        prediction.setScore(predictedScore);
    }

    public void color(int color) {
        setBackgroundColor(color);
        prediction.solidBackground(color, ColorUtil.WHITE);
    }

    public void uncolor() {
        setBackgroundColor(ColorUtil.TRANSPARENT);
        prediction.strokedBackground(ColorUtil.STANDARD_TEXT, ColorUtil.STANDARD_TEXT);
    }
}
