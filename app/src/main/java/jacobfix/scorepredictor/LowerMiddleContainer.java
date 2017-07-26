package jacobfix.scorepredictor;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.util.ViewUtil;

public class LowerMiddleContainer extends FrameLayout {

    private static final String TAG = LowerMiddleContainer.class.getSimpleName();

    private TextView predictionText;
    private SpreadView spread;

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
        predictionText = ViewUtil.findById(this, R.id.prediction_text);
        spread = ViewUtil.findById(this, R.id.spread);
        resize();
    }

    public void resize() {
        predictionText.measure(0, 0);
        getLayoutParams().width = predictionText.getMeasuredWidth();
        requestLayout();
    }

    public void showPredictionText() {
        predictionText.setVisibility(View.VISIBLE);
        spread.setVisibility(View.GONE);
    }

    public void showSpread() {
        predictionText.setVisibility(View.GONE);
        spread.setVisibility(View.VISIBLE);
    }

    public SpreadView getSpreadView() {
        return spread;
    }

    public void setPredictionTextColor(int color) {
        predictionText.setTextColor(color);
    }
}
