package jacobfix.scorepredictor.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.R;
import jacobfix.scorepredictor.util.ViewUtil;

/**
 * Created by jake on 2/19/17.
 */
public class GameClockView extends LinearLayout {

    private TextView mQuarter;
    private TextView mClock;
    private ImageView mLock;

    public GameClockView(Context context) {
        this(context, null);
    }

    public GameClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mQuarter = ViewUtil.findById(this, R.id.quarter);
        mClock = ViewUtil.findById(this, R.id.clock);
        mLock = ViewUtil.findById(this, R.id.lock);
    }
}
