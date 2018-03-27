package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class ScoreboardNameContainer extends LinearLayout {

    private static final String TAG = ScoreboardNameContainer.class.getSimpleName();

    private TextView abbr;
    private TextView name;

    public ScoreboardNameContainer(Context context) {
        this(context, null);
    }

    public ScoreboardNameContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreboardNameContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        this.abbr = ViewUtil.findById(this, R.id.abbr);
        this.name = ViewUtil.findById(this, R.id.name);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        // TODO: Set padding here or in XML?
        // int padding = (int) Util.dpToPx(getContext(), 10);
        // setPadding(padding, 0, padding, 0);

        int desiredWidth = suggestedWidth - (getPaddingLeft() + getPaddingRight());

        abbr.setTypeface(FontHelper.getYantramanavBold(getContext()));
        ViewUtil.fitTextToWidth(abbr, desiredWidth, "WAS");

        name.setTypeface(FontHelper.getYantramanavBold(getContext()));
        ViewUtil.fitTextToWidth(name, desiredWidth, "BUCCANEERS");

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setName(String abbr, String name) {
        this.abbr.setText(abbr);
        this.name.setText(name);
    }
}
