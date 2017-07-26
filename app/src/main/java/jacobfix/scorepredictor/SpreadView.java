package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class SpreadView extends FrameLayout {

    private static final String TAG = SpreadView.class.getSimpleName();

    TextView spread;
    ProgressBar progressBar;

    private static final String BOUNDS_STRING = "22";
    private static final int PADDING = 12;

    public SpreadView(Context context) {
        this(context, null);
    }

    public SpreadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpreadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        spread = ViewUtil.findById(this, R.id.spread);
        progressBar = ViewUtil.findById(this, R.id.spread_progress_bar);

        spread.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        ViewUtil.applyDeboss(spread);
        // ViewUtil.applyDeboss(spread);
        // resize();
        // Log.d(TAG, "" + getLayoutParams().width);
        // Log.d(TAG, "" + getLayoutParams().height);
        // setBackgroundColor(Color.RED);
    }

    public void setSpread(int s) {
        spread.setText(String.valueOf(s));
    }

    public void setProgress(int p) {
        int difference = progressBar.getMax() - p;
        if (difference < 0)
            difference = 0;
        progressBar.setProgress(difference);
    }

    public void setTextColor(int color) {
        spread.setTextColor(color);
    }

    public void setProgressBarColor(int color) {
        progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public void resize() {
        Rect boundingRect = new Rect();
        spread.getPaint().getTextBounds(BOUNDS_STRING, 0, BOUNDS_STRING.length(), boundingRect);
        Log.d(TAG, "Bounding rect width: " + boundingRect.width());
        Log.d(TAG, "Bounding rect height: " + boundingRect.height());
        int length = (boundingRect.width() > boundingRect.height()) ? boundingRect.width() : boundingRect.height();
        // length += 2 * ViewUtil.convertDpToPx(getResources(), PADDING);
        getLayoutParams().width = length;
        getLayoutParams().height = length;
        requestLayout();
        /*
        Log.d(TAG, "About to resize SpreadView");
        spread.measure(0, 0);
        spread.getLayoutParams().height = spread.getLayoutParams().width;
        spread.requestLayout();
        Log.d(TAG, "Just resized SpreadView");
        */
    }
}
