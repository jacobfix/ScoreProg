package jacobfix.scorepredictor;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class LobbyListItemMiddleContainer extends LinearLayout {

    private LinearLayout startTimeContainer;
    private TextView startTime;
    private TextView meridiem;

    private LinearLayout gameTimeContainer;
    private TextView clock;
    private TextView quarter;

    private TextView finalText;

    private ImageView lock;

    public LobbyListItemMiddleContainer(Context context) {
        this(context, null);
    }

    public LobbyListItemMiddleContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LobbyListItemMiddleContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        startTimeContainer = ViewUtil.findById(this, R.id.start_time_container);
        startTime = ViewUtil.findById(this, R.id.start_time);
        meridiem = ViewUtil.findById(this, R.id.meridiem);

        startTime.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        meridiem.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        gameTimeContainer = ViewUtil.findById(this, R.id.game_time_container);
        clock = ViewUtil.findById(this, R.id.clock);
        quarter = ViewUtil.findById(this, R.id.quarter);

        clock.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        quarter.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        finalText = ViewUtil.findById(this, R.id.final_text);

        finalText.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        lock = ViewUtil.findById(this, R.id.lock);
    }

    public void pregameDisplay() {
        startTimeContainer.setVisibility(View.VISIBLE);
        gameTimeContainer.setVisibility(View.GONE);
        finalText.setVisibility(View.GONE);
    }

    public void inProgressDisplay() {
        startTimeContainer.setVisibility(View.GONE);
        gameTimeContainer.setVisibility(View.VISIBLE);
        finalText.setVisibility(View.GONE);
    }

    public void finalDisplay() {
        startTimeContainer.setVisibility(View.GONE);
        gameTimeContainer.setVisibility(View.GONE);
        finalText.setVisibility(View.VISIBLE);
    }

    public void lock() {
        lock.setVisibility(View.VISIBLE);
    }

    public void unlock() {
        lock.setVisibility(View.GONE);
    }

    public void setStartTime(String startTime, String meridiem) {
        this.startTime.setText(startTime);
        this.meridiem.setText(" " + meridiem.toUpperCase());
    }

    public void setClock(String clock) {
        this.clock.setText(clock);
    }

    public void setQuarter(int quarter) {
        String q = Util.formatQuarter(getResources(), quarter);
        this.quarter.setText(q);
    }


}
