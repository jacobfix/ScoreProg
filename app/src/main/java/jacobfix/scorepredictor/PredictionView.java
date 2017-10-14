package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class PredictionView extends FrameLayout {

    private int score = Prediction.NULL;

    private ImageView background;
    private TextView text;

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
        background = ViewUtil.findById(this, R.id.background);
        text = ViewUtil.findById(this, R.id.text);
        text.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        resize();
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

    public TextView getTextView() {
        return text;
    }

    public void refresh() {
        if (score == -1)
            text.setText("");
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
        if (score == 0)
            text.setText("");
        text.append(digit);
        score = Integer.valueOf(text.getText().toString());
        refresh();
    }

    public void clear() {
        score = -1;
        text.setText(R.string.empty_string);
        refresh();
    }

    public void resize() {
        Rect boundingRect = new Rect();
        text.getPaint().getTextBounds(BOUNDS_STRING, 0, BOUNDS_STRING.length(), boundingRect);
        int length = (boundingRect.width() > boundingRect.height()) ? boundingRect.width() : boundingRect.height();
        length += (2 * ViewUtil.convertDpToPx(getResources(), this.padding));
        background.getLayoutParams().width = length;
        background.getLayoutParams().height = length;
        background.requestLayout();
    }
}
