package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class StatsListItemStatNameContainer extends FrameLayout {

    private TextView name;

    public StatsListItemStatNameContainer(Context context) {
        this(context, null);
    }

    public StatsListItemStatNameContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatsListItemStatNameContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        name = ViewUtil.findById(this, R.id.name);
        name.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        name.setText("Passing yards");
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
