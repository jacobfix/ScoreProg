package jacobfix.scorepredictor.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import jacobfix.scorepredictor.R;
import jacobfix.scorepredictor.util.ViewUtil;

public class PredictionListItemPredictionContainer extends FrameLayout {

    private PredictionView prediction;

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
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
