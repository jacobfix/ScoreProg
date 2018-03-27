package jacobfix.scoreprog.components;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ViewUtil;

public class FlipCardView extends FrameLayout {

    private TextView mText;
    private ImageView mCardTopHalf;
    private ImageView mCardBottomHalf;
    private int mPadding;

    // Padding should depend on the text size
    private static final int DEFAULT_PADDING = 4;
    private static final String BOUNDS_STRING = "22";

    public FlipCardView(Context context) {
        this(context, null);
    }

    public FlipCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlipCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPadding = DEFAULT_PADDING;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mCardTopHalf = ViewUtil.findById(this, R.id.card_top_half);
        mCardBottomHalf = ViewUtil.findById(this, R.id.card_bottom_half);
        mText = ViewUtil.findById(this, R.id.text);
        resize();
    }

    public void resize() {
        Rect boundingRect = new Rect();
        mText.getPaint().getTextBounds(BOUNDS_STRING, 0, BOUNDS_STRING.length(), boundingRect);
        int length = (boundingRect.width() > boundingRect.height()) ? boundingRect.width() : boundingRect.height();
        int paddingPx = (int) ViewUtil.convertDpToPx(getResources(), mPadding);
        int width = length + (2 * paddingPx);
        int height = length/2 + paddingPx;
        mCardTopHalf.getLayoutParams().width = width;
        mCardTopHalf.getLayoutParams().height = height;
        mCardBottomHalf.getLayoutParams().width = width;
        mCardBottomHalf.getLayoutParams().height = height;
        mCardTopHalf.requestLayout();
        mCardBottomHalf.requestLayout();
    }
}
