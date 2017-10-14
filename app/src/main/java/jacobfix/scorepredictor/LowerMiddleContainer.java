package jacobfix.scorepredictor;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import jacobfix.scorepredictor.util.ViewUtil;

public class LowerMiddleContainer extends FrameLayout {

    private static final String TAG = LowerMiddleContainer.class.getSimpleName();

    private ProgressBar loadingCircle;
    private TextView predictionText;
    private SpreadView spread;

    private static final String BOUND_STRING = "UNPREDICTED";

    public LowerMiddleContainer(Context context) {
        this(context, null);
    }

    public LowerMiddleContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LowerMiddleContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        loadingCircle = ViewUtil.findById(this, R.id.submit_prediction_pending);
        predictionText = ViewUtil.findById(this, R.id.prediction_text);
        spread = ViewUtil.findById(this, R.id.spread);
        resize();
    }

    public void resize() {
        float width = predictionText.getPaint().measureText(BOUND_STRING);
        getLayoutParams().width = (int) width;
        requestLayout();
    }

    public void showPredictedText() {
        predictionText.setText("Predicted");
        loadingCircle.setVisibility(View.GONE);
        spread.setVisibility(View.GONE);
        predictionText.setVisibility(View.VISIBLE);
    }

    public void showUnpredictedText() {
        predictionText.setText("Unpredicted");
        loadingCircle.setVisibility(View.GONE);
        spread.setVisibility(View.GONE);
        predictionText.setVisibility(View.VISIBLE);
    }

    public void showLoading() {
        loadingCircle.setVisibility(View.VISIBLE);
        spread.setVisibility(View.GONE);
        predictionText.setVisibility(View.GONE);
    }

    public void showSpread() {
        loadingCircle.setVisibility(View.GONE);
        spread.setVisibility(View.VISIBLE);
        predictionText.setVisibility(View.GONE);
    }

    public SpreadView getSpreadView() {
        return spread;
    }

    public void setPredictionTextColor(int color) {
        predictionText.setTextColor(color);
    }
}
