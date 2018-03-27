package jacobfix.scoreprog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.Util;
import jacobfix.scoreprog.util.ViewUtil;

public class UpperMiddleContainer extends LinearLayout {

    private static final String TAG = UpperMiddleContainer.class.getSimpleName();

    private LinearLayout startTimeContainer;
    private TextView startTime;
    private TextView meridiem;
    private TextView gameDate;
    private TextView dayOfWeek;

    private LinearLayout gameTimeContainer;
    private TextView clock;
    private TextView quarter;

    private TextView finalText;

    private String currentBoundsString;

    private static final String BOUNDS_STRING_1 = "mmmm. mm, mm";
    private static final String BOUNDS_STRING_2 = "mm:mm";

    public UpperMiddleContainer(Context context) {
        this(context, null);
    }

    public UpperMiddleContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UpperMiddleContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        startTimeContainer = ViewUtil.findById(this, R.id.start_time_container);
        startTime = ViewUtil.findById(this, R.id.start_time);
        meridiem = ViewUtil.findById(this, R.id.meridiem);
        // dayOfWeek = ViewUtil.findById(this, R.id.day_of_week);
        gameDate = ViewUtil.findById(this, R.id.date);

        // gameTimeContainer = ViewUtil.findById(this, R.id.game_time_container);
        clock = ViewUtil.findById(this, R.id.clock);
        quarter = ViewUtil.findById(this, R.id.quarter);

        finalText = ViewUtil.findById(this, R.id.final_text);

        startTime.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        meridiem.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        gameDate.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        clock.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        quarter.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        finalText.setTypeface(FontHelper.getYantramanavRegular(getContext()));
    }

    public void setStartTime(String time, String meridiem) {
        this.startTime.setText(time);
        this.meridiem.setText(" " + meridiem.toUpperCase());
    }

    public void setDayOfWeek(String dow) {
        this.dayOfWeek.setText(dow);
    }

    public void setDate(String date) {
        this.gameDate.setText(date);
    }

    public void setClock(String clock) {
        this.clock.setText(clock);
    }

    public void setQuarter(int quarter) {
        String t = Util.parseQuarter(quarter);
        this.quarter.setText(t);
    }

    public void pregameDisplay(String localStartTime, boolean pm, Schedule.Day dow, String date) {
        this.startTimeContainer.setVisibility(View.VISIBLE);
        this.gameDate.setVisibility(View.VISIBLE);

        this.clock.setVisibility(View.GONE);
        this.quarter.setVisibility(View.GONE);

        this.finalText.setVisibility(View.GONE);

        String[] splitTime = localStartTime.split(" ");
        this.startTime.setText(splitTime[0]);
        this.meridiem.setText(" " + splitTime[1]);
        this.gameDate.setText(String.format("%s, %s", Util.getDayOfWeekString(dow), date));

        if (currentBoundsString != BOUNDS_STRING_1) {
            currentBoundsString = BOUNDS_STRING_1;
            resize(BOUNDS_STRING_1);
        }
    }

    public void inProgressDisplay(int quarter, String clock) {
        this.startTimeContainer.setVisibility(View.GONE);
        this.gameDate.setVisibility(View.GONE);

        this.clock.setVisibility(View.VISIBLE);
        this.quarter.setVisibility(View.VISIBLE);

        this.finalText.setVisibility(View.GONE);

        this.clock.setText(clock);
        this.quarter.setText(Util.getQuarterString(quarter));

        if (currentBoundsString != BOUNDS_STRING_2) {
            currentBoundsString = BOUNDS_STRING_2;
            resize(BOUNDS_STRING_2);
        }
    }

    public void finalDisplay() {
        this.startTimeContainer.setVisibility(View.GONE);
        this.gameDate.setVisibility(View.GONE);

        this.clock.setVisibility(View.GONE);
        this.quarter.setVisibility(View.GONE);

        this.finalText.setVisibility(View.VISIBLE);

        if (currentBoundsString != BOUNDS_STRING_2) {
            currentBoundsString = BOUNDS_STRING_2;
            resize(BOUNDS_STRING_2);
        }
    }

    public void resize(String bounds) {
        Log.d(TAG, "Resizing UpperMiddleContainer");
        float width = gameDate.getPaint().measureText(bounds);
        getLayoutParams().width = (int) width;
        requestLayout();
    }
}
