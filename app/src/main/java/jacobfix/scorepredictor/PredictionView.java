package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import jacobfix.scorepredictor.util.ViewUtil;

public class PredictionView extends FrameLayout {

    private int mScore = -1;

    private ImageView background;
    private TextView mText;

    private int padding;

    private static final int DEFAULT_PADDING = 6;
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

    public void solidBackground(int textColor, int backgroundColor) {
        mText.setTextColor(textColor);
        background.setImageDrawable(getResources().getDrawable(R.drawable.prediction_square_other));
        background.getDrawable().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN);
        background.setVisibility(View.VISIBLE);
    }

    public void strokedBackground(int textColor, int strokeColor) {
        // mText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        mText.setTextColor(textColor);
        background.setImageDrawable(getResources().getDrawable(R.drawable.prediction_square_new));
        background.getDrawable().setColorFilter(strokeColor, PorterDuff.Mode.SRC_IN);
        background.setVisibility(View.VISIBLE);
    }

    public void noBackground(int textColor) {
        mText.setTextColor(textColor);
        background.setVisibility(View.INVISIBLE);
    }

    public void setScore(int score) {
        mScore = score;
        mText.setText(String.valueOf(score));
        refresh();
    }

    public int getScore() {
        return mScore;
    }

    public TextView getTextView() {
        return mText;
    }

    public void refresh() {
        if (mScore == -1) {
            mText.setText("");
        }
        mText.invalidate();
    }

    public void setColor(int color) {
        // ViewUtil.setTwoLayerRectangleColor(this.background.getDrawable(), color);
        background.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        invalidate();
    }

    public void showBackground() {
        background.setVisibility(View.VISIBLE);
        invalidate();
    }

    public void hideBackground() {
        background.setVisibility(View.INVISIBLE);
        invalidate();
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void append(String digit) {
        if (mScore == 0)
            mText.setText("");
        mText.append(digit);
        mScore = Integer.valueOf(mText.getText().toString());
        refresh();
    }

    public void clear() {
        mScore = -1;
        mText.setText(R.string.empty_string);
        refresh();
    }

    public void setTextSize(int dp) {
        mText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dp);
        resize();
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
