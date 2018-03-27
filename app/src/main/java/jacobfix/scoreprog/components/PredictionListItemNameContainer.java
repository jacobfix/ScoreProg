package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class PredictionListItemNameContainer extends LinearLayout {

    private TextView ranking;
    private TextView username;

    public PredictionListItemNameContainer(Context context) {
        this(context, null);
    }

    public PredictionListItemNameContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PredictionListItemNameContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        ranking = ViewUtil.findById(this, R.id.ranking);
        username = ViewUtil.findById(this, R.id.username);

        ranking.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        username.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        ranking.setTextColor(ColorUtil.STANDARD_TEXT);
        username.setTextColor(ColorUtil.STANDARD_TEXT);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setRanking(int ranking) {
        this.ranking.setText(String.valueOf(ranking));
    }

    public void setUsername(String username) {
        this.username.setText(username);
        // ViewUtil.fitTextToWidth(this.username, getWidth() - getPaddingLeft() - getPaddingRight(),
        //         username);
    }

    public void showRanking() {
        this.ranking.setVisibility(View.VISIBLE);
    }

    public void hideRanking() {
        this.ranking.setVisibility(View.INVISIBLE);
    }
}
