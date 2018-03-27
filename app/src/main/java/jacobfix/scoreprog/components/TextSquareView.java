package jacobfix.scoreprog.components;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ViewUtil;

public class TextSquareView extends FrameLayout {

    private static final String TAG = TextSquareView.class.getSimpleName();

    private TextView mText;
    private ImageView mSquare;

    public static final int DEFAULT_PADDING = 4;
    public static final String BOUNDS_STRING = "22";

    public TextSquareView(Context context) {
        this(context, null);
    }

    public TextSquareView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextSquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mText = ViewUtil.findById(this, R.id.text);
        mSquare = ViewUtil.findById(this, R.id.background);
        Log.d(TAG, "About to resize TextSquareView");
        resize();
    }

    public void resize() {
        resize(DEFAULT_PADDING, BOUNDS_STRING);
    }

    public void resize(int padding, String boundsString) {
        Rect boundingRect = new Rect();
        mText.getPaint().getTextBounds(BOUNDS_STRING, 0, BOUNDS_STRING.length(), boundingRect);
        int length = (boundingRect.width() > boundingRect.height()) ? boundingRect.width() : boundingRect.height();
        int paddingPx = (int) ViewUtil.convertDpToPx(getResources(), padding);
        int width = length + (2 * paddingPx);
        int height = length/2 + paddingPx;
        mSquare.getLayoutParams().width = width;
        mSquare.getLayoutParams().height = height;
        mSquare.requestLayout();
    }
}
