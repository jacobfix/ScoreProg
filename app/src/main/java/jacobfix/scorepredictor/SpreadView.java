package jacobfix.scorepredictor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SpreadView extends LinearLayout {

    int mValue;
    TextView mValueDisplay;
    ImageView mUpArrow;
    ImageView mDownArrow;

    public static final int DOWN = 1;
    public static final int UP = 2;
    public static final int NONE = 3;

    public SpreadView(Context context) {
        this(context, null);
    }

    public SpreadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpreadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mValueDisplay = ViewUtil.findById(this, R.id.value);
        mUpArrow = ViewUtil.findById(this, R.id.up_arrow);
        mDownArrow = ViewUtil.findById(this, R.id.down_arrow);
    }

    public void setValue(int value) {
        mValue = value;
        if (value != 0) {
            mValueDisplay.setText(String.valueOf(value));
        } else {
            mValueDisplay.setText(R.string.dash);
        }
    }

    public int getValue() {
        return mValue;
    }

    public void setState(int state) {
        switch (state) {
            case DOWN:
                mUpArrow.setVisibility(View.INVISIBLE);
                mDownArrow.setVisibility(View.VISIBLE);
                break;
            case UP:
                mUpArrow.setVisibility(View.VISIBLE);
                mDownArrow.setVisibility(View.INVISIBLE);
                break;
            case NONE:
                mUpArrow.setVisibility(View.INVISIBLE);
                mDownArrow.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void compute(NflTeam team) {
        int diff = team.getPredictedScore() - team.getScore();
        if (diff > 0) {
            setValue(diff);
            setState(SpreadView.UP);
        } else if (diff < 0) {
            setValue(-1 * diff);
            setState(SpreadView.DOWN);
        } else {
            setValue(0);
            setState(SpreadView.NONE);
        }
    }

    public TextView getTextView() {
        return mValueDisplay;
    }
}
