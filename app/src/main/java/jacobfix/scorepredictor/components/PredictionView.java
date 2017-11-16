package jacobfix.scorepredictor.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.R;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class PredictionView extends FrameLayout {

    private static final String TAG = PredictionView.class.getSimpleName();

    private int score = Prediction.NULL;

    private ImageView background;
    private TextView text;

    private float sizeRatio = DEFAULT_SIZE_RATIO;

    private int padding;

    private boolean modified;

    private static final int NO_SCORE = -1;
    private static final int MAX_NUMBER_OF_DIGITS = 2;

    public static final float DEFAULT_SIZE_RATIO = 0.4f;
    private static final int BACKGROUND_PADDING = 16;

    public PredictionView(Context context) {
        this(context, null);
    }

    public PredictionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PredictionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        text = ViewUtil.findById(this, R.id.text);
        background = ViewUtil.findById(this, R.id.background);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        text.setTypeface(FontHelper.getArimoRegular(getContext()));

        int backgroundPaddingPx = (int) Util.dpToPx(getContext(), BACKGROUND_PADDING);

        String boundsString = "99";

        /* We want to get the maximum text size without the background padding; we'll add it back in
           later when setting the background size. */
        int desiredWidth = (int) (suggestedWidth * sizeRatio);
        float textSize = ViewUtil.fitTextToWidth(text, desiredWidth, boundsString);
        Log.d(TAG, "PredictionView text size: " + textSize);

        // int desiredWidth = suggestedWidth - getPaddingLeft() - getPaddingRight() - backgroundPaddingPx;
        // ViewUtil.fitTextToWidth(text, desiredWidth, boundsString);

        Rect bounds = new Rect();
        text.getPaint().getTextBounds(boundsString, 0, boundsString.length(), bounds);
        int sideLength = (bounds.width() > bounds.height()) ? bounds.width() : bounds.height();
        sideLength += backgroundPaddingPx;

        background.getLayoutParams().width = sideLength;
        background.getLayoutParams().height = sideLength;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void solidBackground(int textColor, int backgroundColor) {
        text.setTextColor(textColor);
        // background.setImageDrawable(getResources().getDrawable(R.drawable.prediction_square_other));
        background.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.prediction_square_other, null));
        background.getDrawable().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN);
        background.setVisibility(View.VISIBLE);
    }

    public void strokedBackground(int textColor, int strokeColor) {
        text.setTextColor(textColor);
        // background.setImageDrawable(getResources().getDrawable(R.drawable.prediction_square_new));
        background.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.prediction_square_new, null));
        background.getDrawable().setColorFilter(strokeColor, PorterDuff.Mode.SRC_IN);
        background.setVisibility(View.VISIBLE);
    }

    public void noBackground(int textColor) {
        text.setTextColor(textColor);
        background.setVisibility(View.INVISIBLE);
    }

    public void setScore(int s) {
        score = s;
        text.setText(String.valueOf(score));
        refresh();
    }

    public int getScore() {
        return score;
    }

    public void setSizeRatio(float sizeRatio) {
        this.sizeRatio = sizeRatio;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void append(String digit) {
        if (text.length() >= MAX_NUMBER_OF_DIGITS)
            return;

        if (score == 0) {
            /* If the current value in the prediction view is 0, then we want to clear it before
               appending a new digit so that we don't end up with something like "08." */
            text.setText(R.string.empty_string);
        }
        text.append(digit);
        score = Integer.valueOf(text.getText().toString());
        modified = true;
        refresh();
    }

    public void clear() {
        score = NO_SCORE;
        text.setText(R.string.empty_string);
        modified = true;
        refresh();
    }

    public TextView getTextView() {
        return text;
    }

    // TODO: Is this necessary?
    public void refresh() {
        if (score == NO_SCORE)
            text.setText(R.string.empty_string);
        text.invalidate();
    }

    public void setColor(int color) {
        background.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        invalidate();
    }

    public void setTypeface(Typeface typeface) {
        setTypeface(typeface, true);
    }

    public void setTypeface(Typeface typeface, boolean resize) {
        text.setTypeface(typeface);
        if (resize) resize();
    }

    public void setTextSize(int dp) {
        setTextSize(dp, true);
    }

    public void setTextSize(int dp, boolean resize) {
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dp);
        if (resize) resize();
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void resize() {
        Rect boundingRect = new Rect();
        text.getPaint().getTextBounds("99", 0, "99".length(), boundingRect);
        int length = (boundingRect.width() > boundingRect.height()) ? boundingRect.width() : boundingRect.height();
        length += (2 * ViewUtil.convertDpToPx(getResources(), BACKGROUND_PADDING));
        background.getLayoutParams().width = length;
        background.getLayoutParams().height = length;
        background.requestLayout();
    }
}
