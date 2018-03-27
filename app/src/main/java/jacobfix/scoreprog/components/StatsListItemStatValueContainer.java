package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ViewUtil;

public class StatsListItemStatValueContainer extends FrameLayout {

    private TextView stat;

    public StatsListItemStatValueContainer(Context context) {
        this(context, null);
    }

    public StatsListItemStatValueContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatsListItemStatValueContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        stat = ViewUtil.findById(this, R.id.stat);
        stat.setText("405");
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = suggestedWidth - getPaddingLeft() - getPaddingRight();

        ViewUtil.fitTextToWidth(stat, desiredWidth, "999");

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
