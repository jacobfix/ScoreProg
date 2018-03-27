package jacobfix.scoreprog.components;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ViewUtil;

public class MyToolbar extends Toolbar {

    private TextView title;

    public MyToolbar(Context context) {
        this(context, null);
    }

    public MyToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        title = ViewUtil.findById(this, R.id.title);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        int statusBarHeight = ViewUtil.getStatusBarHeight(getContext());

        setPadding(0, statusBarHeight, 0, 0);
        int height = suggestedHeight + statusBarHeight;

        setMeasuredDimension(suggestedWidth, height);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }
}
