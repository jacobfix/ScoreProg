package jacobfix.scoreprog;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;

public class SquareBackgroundTextViewContainer {

    private static final @DrawableRes int DRAWABLE = R.drawable.colored_shadow_rectangle;

    public SquareBackgroundTextViewContainer(Context context) {
        this(context, null);
    }

    public SquareBackgroundTextViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareBackgroundTextViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        // super(context, attrs, defStyleAttr, DRAWABLE);
    }

}

