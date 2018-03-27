package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.Util;
import jacobfix.scoreprog.util.ViewUtil;

public class LobbyListItemNameContainer extends LinearLayout {

    private static final String TAG = LobbyListItemNameContainer.class.getSimpleName();

    private TextView abbr;
    private TextView name;

    public LobbyListItemNameContainer(Context context) {
        this(context, null);
    }

    public LobbyListItemNameContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LobbyListItemNameContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        float leftRightPadding = Util.dpToPx(getContext(), 14);
        TextView abbr = ViewUtil.findById(this, R.id.abbr);
        // Log.d(TAG, "Original text size: " + abbr.getTextSize());
        int desiredWidth = (int) (suggestedWidth - leftRightPadding);
        ViewUtil.fitTextToWidth(abbr, desiredWidth, "WAS");
        // Log.d(TAG, "Final text size: " + abbr.getTextSize());

        TextView name = ViewUtil.findById(this, R.id.name);
        ViewUtil.fitTextToWidth(name, desiredWidth, "BUCCANEERS");

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "Measured list item name container");
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        abbr = ViewUtil.findById(this, R.id.abbr);
        name = ViewUtil.findById(this, R.id.name);

        abbr.setTypeface(FontHelper.getYantramanavBold(getContext()));
        name.setTypeface(FontHelper.getYantramanavBold(getContext()));

        uncolor();

        // ViewUtil.applyDeboss(abbr);
        // ViewUtil.applyDeboss(name);

        Log.d(TAG, "NDS name container onFinishInflate()");
    }

    public void color(int color) {
        setBackgroundColor(color);
         abbr.setTextColor(ColorUtil.WHITE);
         name.setTextColor(ColorUtil.WHITE);
    }

    public void uncolor() {
        setBackgroundColor(ColorUtil.WHITE);
         abbr.setTextColor(ColorUtil.STANDARD_TEXT);
         name.setTextColor(ColorUtil.STANDARD_TEXT);
    }

    public void setAbbr(String abbr) {
         this.abbr.setText(abbr);
    }

    public void setName(String name) {
         this.name.setText(name);
    }
}
