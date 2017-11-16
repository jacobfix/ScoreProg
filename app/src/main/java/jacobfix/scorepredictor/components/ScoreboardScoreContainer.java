package jacobfix.scorepredictor.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.R;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class ScoreboardScoreContainer extends LinearLayout {

    private static final String TAG = ScoreboardScoreContainer.class.getSimpleName();

    private TextView score;

    public ScoreboardScoreContainer(Context context) {
        this(context, null);
    }

    public ScoreboardScoreContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreboardScoreContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        this.score = ViewUtil.findById(this, R.id.score);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        int leftRightPadding = (int) Util.dpToPx(getContext(), 5);
        setPadding(leftRightPadding, 0, leftRightPadding, 0);

        int desiredWidth = suggestedWidth - getPaddingLeft() - getPaddingRight();

        score.setTypeface(FontHelper.getArimoRegular(getContext()));
        ViewUtil.fitTextToWidth(score, desiredWidth, "99");

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setScore(int score) {
        this.score.setText(String.valueOf(score));
    }

    public void showScore() {
        score.setVisibility(View.VISIBLE);
    }

    public void hideScore() {
        score.setVisibility(View.INVISIBLE);
    }
}
