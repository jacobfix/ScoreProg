package jacobfix.scoreprog.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.Game;
import jacobfix.scoreprog.R;
import jacobfix.scoreprog.schedule.Schedule;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.Util;
import jacobfix.scoreprog.util.ViewUtil;

public class LobbyListItemMiddleContainer extends LinearLayout {

    private static final String TAG = LobbyListItemMiddleContainer.class.getSimpleName();

    private LinearLayout startTimeContainer;
    private TextView gameStartTime;
    private TextView gameDate;

    private LinearLayout gameTimeContainer;
    private TextView clock;
    private TextView quarter;

    private TextView finalText;

    private ImageView lock;

    private static float startTimeTextWidth;
    private static float startDateTextWidth;

    private static final String BOUNDS_STRING = Util.TIME_AND_DATE_FORMAT.toPattern();

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
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        /* Resize the text views so that they won't exceed the boundaries of their parent.
           This ensures that regardless of the size used in the XML, the sizes will be adjusted
           to display properly on every screen. */
        String startTimeBoundsString = "00:00 PM";
        String dateBoundsString = "SSS, SS/SS";

        float leftRightPadding = Util.dpToPx(getContext(), 10);

        TextView dateTextView = ViewUtil.findById(this, R.id.game_date);
        float textSize = ViewUtil.fitTextToWidth(dateTextView,
                (int) (suggestedWidth - leftRightPadding), dateBoundsString);

        TextView startTimeTextView = ViewUtil.findById(this, R.id.game_start_time);
        startTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

//        TextView meridiemTextView = ViewUtil.findById(this, R.id.meridiem);
//        meridiemTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
//                textSize - Util.dpToPx(getContext(), 1));

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        startTimeContainer = ViewUtil.findById(this, R.id.start_time_container);
        gameStartTime = ViewUtil.findById(this, R.id.game_start_time);
        gameDate = ViewUtil.findById(this, R.id.game_date);

        gameStartTime.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        gameDate.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        gameTimeContainer = ViewUtil.findById(this, R.id.game_time_container);
        clock = ViewUtil.findById(this, R.id.clock);
        quarter = ViewUtil.findById(this, R.id.quarter);

        clock.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        quarter.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        finalText = ViewUtil.findById(this, R.id.final_text);

        finalText.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        lock = ViewUtil.findById(this, R.id.lock);

        // resize(BOUNDS_STRING);
        // resize();
    }

    public void pregameDisplay(long startTime) {
        String[] timeAndDate = Util.translateMillisSinceEpochToLocalDateString(startTime, Util.TIME_AND_DATE_FORMAT).split("~");

        setVisibleComponents(View.VISIBLE, View.GONE, View.GONE);
        this.gameStartTime.setText(timeAndDate[0]);
        this.gameDate.setText(timeAndDate[1]);
//        String[] splitStartTime = localStartTime.split(" ");
//        this.startTime.setText(splitStartTime[0]);
//        this.meridiem.setText(" " + splitStartTime[1]);
//        this.date.setText(String.format("%s, %s", Util.getDayOfWeekString(dow), date));
    }

    public void inProgressDisplay(int quarter, String clock) {
        setVisibleComponents(View.GONE, View.VISIBLE, View.GONE);
        this.clock.setText(clock);
        this.quarter.setText(Util.getQuarterString(quarter));
    }

    public void finalDisplay() {
        setVisibleComponents(View.GONE, View.GONE, View.VISIBLE);
    }

    private void setVisibleComponents(int startTimeContainerVisibility,
                                      int gameTimeContainerVisibility, int finalTextVisibility) {
        startTimeContainer.setVisibility(startTimeContainerVisibility);
        gameTimeContainer.setVisibility(gameTimeContainerVisibility);
        finalText.setVisibility(finalTextVisibility);
    }

    public void lock() {
        lock.setVisibility(View.VISIBLE);
    }

    public void unlock() {
        lock.setVisibility(View.GONE);
    }

//    public void setStartTime(String startTime) {
//        this.startTime.setText(startTime);
//        this.meridiem.setText("");
//    }

//    public void setStartTime(String startTime, String meridiem) {
//        this.startTime.setText(startTime);
//        this.meridiem.setText(" " + meridiem.toUpperCase());
//    }

    public void setClock(String clock) {
        this.clock.setText(clock);
    }

    public void setQuarter(int quarter) {
        String q = Util.formatQuarter(getResources(), quarter);
        this.quarter.setText(q);
    }

    public void resize(String bounds) {
        Log.d(TAG, "Resizing lobby list item middle container");
        float width = gameStartTime.getPaint().measureText(bounds);
        getLayoutParams().width = (int) width;
        requestLayout();
    }

    public void resize() {
        Log.d(TAG, "Resizing..");
        Rect bounds = new Rect();
        Paint paint = new Paint();
        paint.setTypeface(gameDate.getTypeface());
        float textSize = gameDate.getTextSize();
        paint.setTextSize(textSize);

        float desiredWidth = getWidth(); // - 2 * Util.dpToPx(getContext(), 5);
        Log.d(TAG, "Desired width: " + desiredWidth);

        return;
//        String boundsString = "WWW, WW/WW";
//        paint.getTextBounds(boundsString, 0, boundsString.length(), bounds);
//
//        while (bounds.width() > desiredWidth) {
//            textSize--;
//            Log.d(TAG, "" + textSize);
//            paint.setTextSize(textSize);
//            paint.getTextBounds(boundsString, 0, boundsString.length(), bounds);
//        }
//
//        date.setTextSize(paint.getTextSize());
    }
}
