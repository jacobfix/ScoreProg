package jacobfix.scoreprog;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;

public class CircleBackgroundTextViewContainer {

    private static final @DrawableRes int DRAWABLE = R.drawable.background_circle;

    public CircleBackgroundTextViewContainer(Context context) {
        this(context, null);
    }

    public CircleBackgroundTextViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleBackgroundTextViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        // super(context, attrs, defStyleAttr, DRAWABLE);
    }
}
