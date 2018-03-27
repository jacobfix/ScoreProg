package jacobfix.scoreprog;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import jacobfix.scoreprog.util.Util;

public class AutoSizeTextView extends TextView {

    public AutoSizeTextView(Context context) {
        super(context);
        init();
    }

    public AutoSizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoSizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                autoSize();
            }
        });
    }

    private void autoSize() {
        int targetWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        while (true) {
            Rect boundingRect = new Rect();
            getPaint().getTextBounds(getText().toString(), 0, getText().length(), boundingRect);
            if (targetWidth > boundingRect.width())
                break;

            setTextSize(Util.pxToDp(getContext(), getTextSize()) - 1);
        }
    }
}
