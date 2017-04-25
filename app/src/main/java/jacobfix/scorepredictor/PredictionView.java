package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import jacobfix.scorepredictor.util.ViewUtil;

public class PredictionView extends FrameLayout {

    private ImageView background;
    private TextView mText;

    private PredictableScorer mScorer;

    private @DrawableRes int drawable;
    private int padding;

    private static final int DEFAULT_PADDING = 12;
    private static final String BOUNDS_STRING = "22";

    public PredictionView(Context context) {
        this(context, null);
    }

    public PredictionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PredictionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.padding = DEFAULT_PADDING;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        this.background = ViewUtil.findById(this, R.id.background);
        mText = ViewUtil.findById(this, R.id.text);
        resize();
    }

    public TextView getTextView() {
        return mText;
    }

    public void setScorer(PredictableScorer scorer) {
        mScorer= scorer;
        // Basically do the refresh here?
    }

    public PredictableScorer getScorer() {
        return mScorer;
    }

    public void refresh() {
        if (mScorer.getPredictedScore() != -1) {
            mText.setText(String.valueOf(mScorer.getPredictedScore()));
        }
        mText.invalidate();
    }

    public void setColor(int color) {
        // ViewUtil.setTwoLayerRectangleColor(this.background.getDrawable(), color);
        this.background.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        invalidate();
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void append(String digit) {
        if (mScorer.getPredictedScore() == 0) {
            mText.setText(R.string.empty_string);
        }
        mText.append(digit);
        mScorer.setPredictedScore(Integer.parseInt(mText.getText().toString()));
        refresh();
    }

    public void clear() {
        mText.setText(R.string.empty_string);
        mScorer.setPredictedScore(PredictableScorer.NO_PREDICTION);
        refresh();
    }

    public void resize() {
        Rect boundingRect = new Rect();
        mText.getPaint().getTextBounds(BOUNDS_STRING, 0, BOUNDS_STRING.length(), boundingRect);
        int length = (boundingRect.width() > boundingRect.height()) ? boundingRect.width() : boundingRect.height();
        length += (2 * ViewUtil.convertDpToPx(getResources(), this.padding));
        background.getLayoutParams().width = length;
        background.getLayoutParams().height = length;
        background.requestLayout();
    }
}
