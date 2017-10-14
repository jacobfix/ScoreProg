package jacobfix.scorepredictor;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class UpperMiddleContainer extends LinearLayout {

    private static final String TAG = UpperMiddleContainer.class.getSimpleName();

    private LinearLayout startTimeContainer;
    private TextView startTime;
    private TextView meridiem;
    private TextView dayOfWeek;
    private TextView date;

    private LinearLayout gameTimeContainer;
    private TextView clock;
    private TextView quarter;

    private TextView finalText;

    private static String currentBoundsString;

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
        date = ViewUtil.findById(this, R.id.date);

        // gameTimeContainer = ViewUtil.findById(this, R.id.game_time_container);
        clock = ViewUtil.findById(this, R.id.clock);
        quarter = ViewUtil.findById(this, R.id.quarter);

        finalText = ViewUtil.findById(this, R.id.final_text);

        startTime.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        meridiem.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        date.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        clock.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        quarter.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        finalText.setTypeface(FontHelper.getYantramanavRegular(getContext()));
    }

    public void setStartTime(String time, String meridiem) {
        this.startTime.setText(time);
        this.meridiem.setText(" " + meridiem.toUpperCase());
    }

    public void setDayOfWeek(String dow) {
        dayOfWeek.setText(dow);
    }

    public void setDate(String date) {
        this.date.setText(date);
    }

    public void setClock(String clock) {
        this.clock.setText(clock);
    }

    public void setQuarter(int quarter) {
        String t = Util.parseQuarter(quarter);
        this.quarter.setText(t);
    }

    public void pregameDisplay(FullGame game) {
        Log.d(TAG, "UpperMiddleContainer pregame display");
        startTimeContainer.setVisibility(View.VISIBLE);
        date.setVisibility(View.VISIBLE);

        clock.setVisibility(View.GONE);
        quarter.setVisibility(View.GONE);

        finalText.setVisibility(View.GONE);

        if (currentBoundsString != BOUNDS_STRING_1) {
            currentBoundsString = BOUNDS_STRING_1;
            resize(BOUNDS_STRING_1);
        }

        // TODO: Display start times in LOCAL TIME ZONE
        String[] split = game.getStartTimeDisplay().split(" ");
        startTime.setText(split[0]);
        meridiem.setText(" " + split[1]);
        date.setText(String.format("%s, %s", Util.getDayOfWeekString(game.getDayOfWeek()), Util.getDateString(game.getId())));
    }

    public void inProgressDisplay(FullGame game) {
        Log.d(TAG, "UpperMiddleContainer in progress display");
        startTimeContainer.setVisibility(View.GONE);
        date.setVisibility(View.GONE);

        clock.setVisibility(View.VISIBLE);
        quarter.setVisibility(View.VISIBLE);

        finalText.setVisibility(View.GONE);

        if (currentBoundsString != BOUNDS_STRING_2) {
            currentBoundsString = BOUNDS_STRING_2;
            resize(BOUNDS_STRING_2);
        }

        clock.setText(game.getClock());
        quarter.setText(Util.getQuarterString(game.getQuarter()));
    }

    public void finalDisplay(FullGame game) {
        Log.d(TAG, "UpperMiddleContainer final display");
        startTimeContainer.setVisibility(View.GONE);
        date.setVisibility(View.GONE);

        clock.setVisibility(View.GONE);
        quarter.setVisibility(View.GONE);

        finalText.setVisibility(View.VISIBLE);

        if (currentBoundsString != BOUNDS_STRING_2) {
            currentBoundsString = BOUNDS_STRING_2;
            resize(BOUNDS_STRING_2);
        }
    }

    public void resize(String bounds) {
        Log.d(TAG, "Resizing UpperMiddleContainer");
        float width = date.getPaint().measureText(bounds);
        getLayoutParams().width = (int) width;
        requestLayout();
    }
}
