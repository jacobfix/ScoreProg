package jacobfix.scorepredictor.components;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import jacobfix.scorepredictor.R;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class ScoreboardGameStateContainer extends LinearLayout {

    private static final String TAG = ScoreboardGameStateContainer.class.getSimpleName();

    private TextView gameStartTime;
    private TextView gameDate;

    private TextView clock;
    private TextView quarter;

    private TextView finalLabel;

    private ImageView lock;

    private static final SimpleDateFormat TIME_AND_DATE_FORMAT
            = new SimpleDateFormat("h:mm a~E, M/d", Locale.US);

    public ScoreboardGameStateContainer(Context context) {
        this(context, null);
    }

    public ScoreboardGameStateContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreboardGameStateContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public enum State {
        PREGAME,
        IN_PROGRESS,
        FINAL
    }


    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        this.gameStartTime = ViewUtil.findById(this, R.id.game_start_time);
        this.gameDate = ViewUtil.findById(this, R.id.game_date);

        this.clock = ViewUtil.findById(this, R.id.clock);
        this.quarter = ViewUtil.findById(this, R.id.quarter);

        this.finalLabel = ViewUtil.findById(this, R.id.final_label);

        this.lock = ViewUtil.findById(this, R.id.lock);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "Suggested width, suggested height: (" + suggestedWidth + ", " + suggestedHeight + ")");
        Log.d(TAG, "Padding (left, right): (" + getPaddingLeft() + ", " + getPaddingRight() + ")");
        int desiredWidth = suggestedWidth - getPaddingLeft() - getPaddingRight();

        gameStartTime.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        gameDate.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        ViewUtil.fitTextToWidth(gameStartTime, desiredWidth, "99:99 MM");
        ViewUtil.fitTextToWidth(gameDate, desiredWidth, "MMM, 99/99");

        clock.setTypeface(FontHelper.getPlayRegular(getContext()));
        quarter.setTypeface(FontHelper.getPlayRegular(getContext()));
        ViewUtil.fitTextToWidth(clock, desiredWidth, "99:99");
        ViewUtil.fitTextToWidth(quarter, desiredWidth, "9mm");

        finalLabel.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        ViewUtil.fitTextToWidth(finalLabel, desiredWidth, "FINAL");

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void showLock() {
        lock.setVisibility(View.VISIBLE);
    }

    public void hideLock() {
        lock.setVisibility(View.GONE);
    }

    public void pregameDisplay(long startTime) {
        String[] timeAndDate = Util.translateMillisSinceEpochToLocalDateString(startTime,
                TIME_AND_DATE_FORMAT).split("~");

        this.gameStartTime.setText(timeAndDate[0]);
        this.gameDate.setText(timeAndDate[1]);

        this.gameStartTime.setVisibility(View.VISIBLE);
        this.gameDate.setVisibility(View.VISIBLE);

        this.clock.setVisibility(View.GONE);
        this.quarter.setVisibility(View.GONE);

        this.finalLabel.setVisibility(View.GONE);
    }

    public void inProgressDisplay(String clock, int quarter) {
        this.clock.setText(clock);
        this.quarter.setText(Util.parseQuarter(quarter));

        this.gameStartTime.setVisibility(View.GONE);
        this.gameDate.setVisibility(View.GONE);

        this.clock.setVisibility(View.VISIBLE);
        this.quarter.setVisibility(View.VISIBLE);

        this.finalLabel.setVisibility(View.GONE);
    }

    public void finalDisplay() {
        this.gameStartTime.setVisibility(View.GONE);
        this.gameDate.setVisibility(View.GONE);

        this.clock.setVisibility(View.GONE);
        this.quarter.setVisibility(View.GONE);

        this.finalLabel.setVisibility(View.VISIBLE);
    }

    public int getNecessaryHeight() {
        int necessaryHeight = 0;

        Rect bounds = new Rect();
        Paint paint = new Paint();

        paint.setTextSize(clock.getTextSize());
        paint.setTypeface(clock.getTypeface());
        paint.getTextBounds("0", 0, 1, bounds);
        necessaryHeight += bounds.height();

        paint.setTextSize(quarter.getTextSize());
        paint.setTypeface(quarter.getTypeface());
        paint.getTextBounds("0", 0, 1, bounds);
        necessaryHeight += bounds.height();

        lock.getHeight();

        return necessaryHeight;
    }
}
